package my.project.bookingservice.dto.kafka;

import my.project.bookingservice.dto.response.DishResponse;
import my.project.bookingservice.dto.response.TableResponse;
import my.project.bookingservice.entity.BookingStatus;

import java.time.Instant;
import java.util.List;

public record BookingCreatedEvent(
		Instant startAt,
		Instant endAt,
		BookingStatus status,
		Integer guests,
		String comment,
		TableResponse table,
		List<DishResponse> dishes
) {}
