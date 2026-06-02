package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.client.RestaurantServiceClient;
import my.project.bookingservice.client.UserServiceClient;
import my.project.bookingservice.dto.client.BookingSnapshotRequest;
import my.project.bookingservice.dto.client.UserBriefDto;
import my.project.bookingservice.dto.request.CancelBookingRequest;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.kafka.KafkaProducer;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.context.PricingContextFactory;
import my.project.bookingservice.pricing.dto.request.PricingOfferRequest;
import my.project.bookingservice.pricing.dto.request.PricingPreorderItemRequest;
import my.project.bookingservice.pricing.offer.PricingOfferUsageService;
import my.project.bookingservice.pricing.persistence.entity.PricingOfferEntity;
import my.project.bookingservice.repository.BookingRepository;
import my.project.common.exception.ConflictException;
import my.project.common.exception.NotFoundException;
import my.project.common.exception.ValidationException;
import my.project.common.security.AuthUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

	private final BookingRepository repository;
	private final BookingMapper mapper;
	private final BookingReadService readService;
	private final BookingHelper helper;

	private final RestaurantServiceClient restaurantClient;
	private final UserServiceClient userServiceClient;
	private final KafkaProducer kafkaProducer;
	private final PricingContextFactory pricingContextFactory;
	private final PricingOfferUsageService pricingOfferUsageService;

	@Transactional
	public BookingResponse create(CreateBookingRequest req, Authentication auth) {
		var userId = AuthUtil.id(auth);

		log.info("Создание бронирования, userId={}, restaurantId={}, tableId={}", userId, req.restaurantId(), req.tableId());

		var entity = createBookingEntity(req, userId);
		var savedEntity = save(entity);

		log.info("Бронирование успешно создано, bookingId={}, userId={}", savedEntity.getId(), userId);
		sendBookingCreatedEvent(savedEntity, auth);

		return mapper.toResponse(savedEntity);
	}

	private BookingEntity createBookingEntity(CreateBookingRequest req, UUID userId) {
		Map<UUID, Integer> quantities = req.dishesQuantities();
		var bookingSnapshot = restaurantClient.bookingSnapshot(
				req.restaurantId(),
				new BookingSnapshotRequest(req.tableId(), quantities.keySet())
		);

		if (bookingSnapshot.table().capacity() < req.guests()) {
			log.warn("Невозможно создать бронирование: количество гостей превышает вместимость стола, restaurantId={}, tableId={}",
					req.restaurantId(), req.tableId());
			throw new ConflictException("booking.table.guest-more-capacity");
		}

		BookingEntity entity = helper.buildBookingEntity(req, userId, bookingSnapshot, quantities);
		applyPricing(req, userId, entity);
		return entity;
	}

	private void applyPricing(CreateBookingRequest req, UUID userId, BookingEntity entity) {
		if (!hasPreorder(req)) {
			entity.setPricingOfferId(null);
			entity.setPreorderAmount(zeroMoney());
			entity.setPricingCharge(zeroMoney());
			entity.setTotalAmount(zeroMoney());
			return;
		}

		if (req.pricingOfferId() == null) {
			throw new ValidationException("pricing.offer.required");
		}

		PricingOfferRequest pricingRequest = toPricingOfferRequest(req);
		PricingContext context = pricingContextFactory.create(userId, pricingRequest);
		PricingOfferEntity offer = pricingOfferUsageService.validateAndUse(context, req.pricingOfferId());
		entity.setPricingOfferId(offer.getId());
		entity.setPreorderAmount(offer.getPreorderAmount());
		entity.setPricingCharge(offer.getPricingCharge());
		entity.setTotalAmount(offer.getTotalAmount());
	}

	private PricingOfferRequest toPricingOfferRequest(CreateBookingRequest req) {
		List<PricingPreorderItemRequest> preorderItems = req.dishes() == null
				? List.of()
				: req.dishes().stream()
				.map(item -> new PricingPreorderItemRequest(item.dishId(), item.quantity()))
				.toList();

		return new PricingOfferRequest(
				req.restaurantId(),
				req.tableId(),
				req.startAt(),
				req.endAt(),
				preorderItems
		);
	}

	private boolean hasPreorder(CreateBookingRequest req) {
		return req.dishes() != null && !req.dishes().isEmpty();
	}

	private BigDecimal zeroMoney() {
		return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
	}

	private void sendBookingCreatedEvent(BookingEntity entity, Authentication auth) {
		var email = AuthUtil.email(auth);
		var username = AuthUtil.username(auth);
		kafkaProducer.sendBookingCreated(mapper.toEvent(entity, email, username));
	}

	@Transactional
	public BookingEntity save(BookingEntity booking) {
		try {
			BookingEntity saved = repository.saveAndFlush(booking);
			log.info("Бронирование сохранено в базе данных, bookingId={}", saved.getId());
			return saved;
		} catch (DataIntegrityViolationException e) {
			log.warn("Конфликт при сохранении бронирования: пересечение по времени, tableId={}", booking.getTableId(), e);
			throw new ConflictException("booking.overlap");
		}
	}

	@Transactional
	public void cancel(UUID id, CancelBookingRequest req, Authentication auth) {
		var now = Instant.now();

		log.info("Отмена бронирования, bookingId={}", id);

		BookingEntity booking = readService.findByAuth(id, auth);

		if (booking.isCancelled()) {
			log.info("Бронирование уже было отменено ранее, bookingId={}", id);
			return;
		}

		boolean cancelledByManagerOrAdmin = AuthUtil.isManager(auth) || AuthUtil.isAdmin(auth);
		String reason = req == null ? null : req.reason();

		if (cancelledByManagerOrAdmin && reason == null) {
			log.warn("Причина отмены бронирования менеджером или администратором не указана, bookingId={}", id);
			throw new ValidationException("booking.cancel.reason-required");
		}

		booking.cancel(now, reason);
		repository.saveAndFlush(booking);

		log.info("Бронирование помечено как отменённое, bookingId={}", id);

		if (cancelledByManagerOrAdmin) {
			sendBookingCancelledEvent(booking, reason);
		}
	}

	private void sendBookingCancelledEvent(BookingEntity booking, String reason) {
		UserBriefDto user = userServiceClient.getBriefs(Set.of(booking.getUserId()))
				.stream()
				.findFirst()
				.orElseThrow(() -> {
					log.warn("Пользователь бронирования не найден, bookingId={}, userId={}",
							booking.getId(), booking.getUserId());
					return new NotFoundException("user.not-found-by-id", booking.getUserId());
				});

		String username = user.name() + " " + user.surname();

		kafkaProducer.sendBookingCancelled(mapper.toCancelledEvent(booking, user.email(), username, reason));
	}

}
