package my.project.restaurantservice.dto;

import jakarta.validation.constraints.AssertTrue;
import my.project.restaurantservice.entity.enums.WeekDay;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;

public record WorkingHoursDto(

		@NotNull
		WeekDay dayOfWeek,

		LocalTime openTime,
		LocalTime closeTime,

		boolean closed
) {

	@AssertTrue(message = "Если closed=true, openTime/closeTime должны быть null. Если closed=false, openTime/closeTime обязательны и openTime < closeTime.")
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

