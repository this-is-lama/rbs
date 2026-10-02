package my.project.restaurantservice.workinghours.dto;

import jakarta.validation.constraints.NotNull;
import my.project.restaurantservice.workinghours.entity.WeekDay;

import java.time.LocalTime;

public record WorkingHoursDto(

		@NotNull
		WeekDay dayOfWeek,

		LocalTime openTime,
		LocalTime closeTime,

		boolean closed
) {}
