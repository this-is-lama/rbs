package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.client.RestaurantServiceClient;
import my.project.bookingservice.dto.DishDto;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.mapper.BookingDishMapper;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.bookingservice.repository.BookingRepository;
import my.project.common.security.AuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

	private final BookingRepository repository;
	private final RestaurantServiceClient restaurantClient;
	private final BookingMapper bookingMapper;
	private final BookingDishMapper dishMapper;

	@Transactional
	public BookingResponse create(CreateBookingRequest req, Authentication auth) {
		var userId = AuthUtil.id(auth);
		var dishesSnapshot = restaurantClient.findAllByIds(req.restaurantId(),req.getDishesIds());

		var dishesById = dishesSnapshot.stream().collect(Collectors.toMap(DishDto::id, d -> d));

		BookingEntity entity = bookingMapper.toEntity(req, userId, dishesById, dishMapper);

		entity = repository.save(entity);
		return bookingMapper.toResponse(entity);
	}

}
