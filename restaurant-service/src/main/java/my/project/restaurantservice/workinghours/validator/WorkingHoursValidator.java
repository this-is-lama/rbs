package my.project.restaurantservice.workinghours.validator;

import my.project.common.exception.ValidationException;
import my.project.restaurantservice.workinghours.dto.WorkingHoursDto;
import my.project.restaurantservice.workinghours.entity.WeekDay;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class WorkingHoursValidator {


	public void validate(List<WorkingHoursDto> workingHours) {
		if (workingHours == null) {
			return;
		}
		workingHours.stream()
				.filter(Objects::nonNull)
				.forEach(this::validateDay);
		validateUniqueDays(workingHours);
	}


	public void validateDay(WorkingHoursDto dto) {
		if (!isConsistent(dto)) {
			throw new ValidationException("restaurant.workinghours.invalid");
		}
	}

	public void validateUniqueDays(List<WorkingHoursDto> workingHours) {
		if (workingHours == null) {
			return;
		}
		List<WeekDay> days = workingHours.stream()
				.filter(Objects::nonNull)
				.map(WorkingHoursDto::dayOfWeek)
				.filter(Objects::nonNull)
				.toList();
		if (days.size() != Set.copyOf(days).size()) {
			throw new ValidationException("restaurant.workinghours.duplicate-day");
		}
	}

	private boolean isConsistent(WorkingHoursDto dto) {
		if (dto.closed()) {
			return dto.openTime() == null && dto.closeTime() == null;
		}
		if (dto.openTime() == null || dto.closeTime() == null) {
			return false;
		}
		return dto.openTime().isBefore(dto.closeTime());
	}
}
