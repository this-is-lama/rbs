package my.project.restaurantservice.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import my.project.restaurantservice.restaurant.dto.contact.ContactDto;
import my.project.restaurantservice.restaurant.dto.workinghours.WorkingHoursDto;
import my.project.restaurantservice.restaurant.entity.WeekDay;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RestaurantDto {

	UUID id;

	@NotBlank
	@Size(max = 255)
	String name;

	@NotBlank
	@Size(max = 100)
	String category;

	@Size(max = 2000)
	String description;

	@NotBlank
	@Size(max = 255)
	String address;

	Boolean active;

	@NotEmpty
	@NotNull
	List<@Valid WorkingHoursDto> workingHours;

	@NotEmpty
	@NotNull
	List<@Valid ContactDto> contacts;

	@JsonIgnore
	@AssertTrue(message = "restaurant.workinghours.duplicate-day")
	public boolean isWorkingHoursDaysUnique() {
		if (workingHours == null) {
			return true;
		}
		List<WeekDay> days = workingHours.stream()
				.filter(Objects::nonNull)
				.map(WorkingHoursDto::dayOfWeek)
				.filter(Objects::nonNull)
				.toList();
		return days.size() == Set.copyOf(days).size();
	}
}
