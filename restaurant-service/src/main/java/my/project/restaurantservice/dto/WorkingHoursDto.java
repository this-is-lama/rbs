package my.project.restaurantservice.dto;

import my.project.restaurantservice.entity.WeekDay;

import java.time.LocalTime;

public record WorkingHoursDto(
        WeekDay dayOfWeek,
        LocalTime openTime,
        LocalTime closeTime,
        boolean closed
) {}
