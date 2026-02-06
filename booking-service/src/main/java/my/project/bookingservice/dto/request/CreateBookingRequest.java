package my.project.bookingservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateBookingRequest(

		@NotNull
		UUID restaurantId,

		@NotNull
		UUID tableId,

		@NotNull
		Instant startAt,

		@NotNull
		Instant endAt,

		@NotNull
		@Min(1)
		@Max(50)
		Integer guests,

		@Size(max = 500)
		String comment,

		@Valid
		@Size(max = 50)
		List<BookingDishCreateRequest> dishes

) {

	@JsonIgnore
	@AssertTrue(message = "Некорректное время бронирования: должно быть минимум за 1 час до начала и длительность не меньше 1 часа")
	public boolean isConsistent() {
		if (startAt == null || endAt == null) return true;

		Instant start = startAt();
		Instant end = endAt();
		Instant now = Instant.now();

		boolean correctOrder = start.isBefore(end);

		boolean oneHourBeforeStart = !start.isBefore(now.plus(Duration.ofHours(1)));

		Duration dur = Duration.between(start, end);
		boolean minDurationOneHour = dur.compareTo(Duration.ofHours(1)) >= 0;

		return correctOrder && oneHourBeforeStart && minDurationOneHour;
	}

	@JsonIgnore
	public List<UUID> getDishesIds() {
		if (dishes == null || dishes.isEmpty()) {
			return List.of();
		}
		return dishes.stream()
				.map(BookingDishCreateRequest::dishId)
				.distinct()
				.toList();
	}


}
