package my.project.restaurantservice.restaurant.dto.workinghours;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import my.project.restaurantservice.restaurant.entity.WeekDay;

import java.time.LocalTime;

public record WorkingHoursDto(

		@NotNull
		WeekDay dayOfWeek,

		LocalTime openTime,
		LocalTime closeTime,

		boolean closed
) {
	@JsonIgnore
	@AssertTrue(message = "restaurant.workinghours.invalid")
	public boolean isConsistent() {
		if (closed) {
			return openTime == null && closeTime == null;
		}
		if (openTime == null || closeTime == null) {
			return false;
		}
		return openTime.isBefore(closeTime);
	}
}

