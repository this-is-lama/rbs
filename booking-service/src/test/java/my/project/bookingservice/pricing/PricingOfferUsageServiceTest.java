package my.project.bookingservice.pricing;

import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingOfferStatus;
import my.project.bookingservice.pricing.offer.PricingOfferExpirationService;
import my.project.bookingservice.pricing.offer.PricingOfferUsageService;
import my.project.bookingservice.pricing.persistence.entity.PricingOfferEntity;
import my.project.bookingservice.pricing.persistence.repository.PricingOfferRepository;
import my.project.common.exception.ConflictException;
import my.project.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static my.project.bookingservice.pricing.PricingTestSupport.contextWithPreorder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PricingOfferUsageServiceTest {
	private final PricingOfferRepository repository = mock(PricingOfferRepository.class);
	private final PricingOfferUsageService service = new PricingOfferUsageService(
			repository,
			new PricingOfferExpirationService()
	);

	@Test
	void activeOfferIsMarkedUsed() {
		PricingContext context = contextWithPreorder(BigDecimal.TEN);
		PricingOfferEntity offer = offer(context, PricingOfferStatus.ACTIVE, Instant.now().plusSeconds(600));
		when(repository.findById(offer.getId())).thenReturn(Optional.of(offer));
		when(repository.save(offer)).thenReturn(offer);

		PricingOfferEntity result = service.validateAndUse(context, offer.getId());

		assertThat(result.getStatus()).isEqualTo(PricingOfferStatus.USED);
		verify(repository).save(offer);
	}

	@Test
	void usedOrExpiredOfferIsRejected() {
		PricingContext context = contextWithPreorder(BigDecimal.TEN);

		for (PricingOfferStatus status : List.of(PricingOfferStatus.USED, PricingOfferStatus.EXPIRED)) {
			PricingOfferEntity offer = offer(context, status, Instant.now().plusSeconds(600));
			when(repository.findById(offer.getId())).thenReturn(Optional.of(offer));

			assertThatThrownBy(() -> service.validateAndUse(context, offer.getId()))
					.isInstanceOf(ConflictException.class);
		}
	}

	@Test
	void expiredActiveOfferIsMarkedExpiredAndRejected() {
		PricingContext context = contextWithPreorder(BigDecimal.TEN);
		PricingOfferEntity offer = offer(context, PricingOfferStatus.ACTIVE, Instant.now().minusSeconds(1));
		when(repository.findById(offer.getId())).thenReturn(Optional.of(offer));
		when(repository.save(offer)).thenReturn(offer);

		assertThatThrownBy(() -> service.validateAndUse(context, offer.getId()))
				.isInstanceOf(ConflictException.class);

		assertThat(offer.getStatus()).isEqualTo(PricingOfferStatus.EXPIRED);
		verify(repository).save(offer);
	}

	@Test
	void nullOfferIdIsRejected() {
		PricingContext context = contextWithPreorder(BigDecimal.TEN);

		assertThatThrownBy(() -> service.validateAndUse(context, null))
				.isInstanceOf(ValidationException.class);
	}

	@Test
	void contextMismatchIsRejected() {
		PricingContext context = contextWithPreorder(BigDecimal.TEN);
		List<Consumer<PricingOfferEntity>> mismatches = List.of(
				offer -> offer.setUserId(UUID.randomUUID()),
				offer -> offer.setRestaurantId(UUID.randomUUID()),
				offer -> offer.setTableId(UUID.randomUUID()),
				offer -> offer.setVisitStart(context.visitStart().plusSeconds(60)),
				offer -> offer.setVisitEnd(context.visitEnd().plusSeconds(60)),
				offer -> offer.setCartHash("another-cart")
		);

		for (Consumer<PricingOfferEntity> mismatch : mismatches) {
			PricingOfferEntity offer = offer(context, PricingOfferStatus.ACTIVE, Instant.now().plusSeconds(600));
			mismatch.accept(offer);
			when(repository.findById(offer.getId())).thenReturn(Optional.of(offer));

			assertThatThrownBy(() -> service.validateAndUse(context, offer.getId()))
					.isInstanceOf(ValidationException.class);
		}
	}

	private PricingOfferEntity offer(PricingContext context, PricingOfferStatus status, Instant expiresAt) {
		PricingOfferEntity offer = new PricingOfferEntity();
		offer.setId(UUID.randomUUID());
		offer.setUserId(context.userId());
		offer.setRestaurantId(context.restaurantId());
		offer.setTableId(context.tableId());
		offer.setCartHash(context.cartHash());
		offer.setVisitStart(context.visitStart());
		offer.setVisitEnd(context.visitEnd());
		offer.setStatus(status);
		offer.setExpiresAt(expiresAt);
		offer.setCreatedAt(Instant.now());
		offer.setPreorderAmount(context.preorderAmount());
		offer.setPricingCharge(BigDecimal.ONE);
		offer.setTotalAmount(context.preorderAmount().add(BigDecimal.ONE));
		offer.setVersion(0L);
		return offer;
	}
}
