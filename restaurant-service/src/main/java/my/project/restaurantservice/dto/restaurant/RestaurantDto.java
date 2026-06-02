package my.project.restaurantservice.dto.restaurant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import my.project.restaurantservice.dto.contact.ContactDto;
import my.project.restaurantservice.dto.dish.DishDto;
import my.project.restaurantservice.dto.photo.PhotoDto;
import my.project.restaurantservice.dto.table.TableDto;
import my.project.restaurantservice.dto.workinghours.WorkingHoursDto;

import java.math.BigDecimal;
import java.util.List;
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

	BigDecimal minPricingCharge;

	BigDecimal maxPricingCharge;

	@NotEmpty
	@NotNull
	List<@Valid WorkingHoursDto> workingHours;

	@NotEmpty
	@NotNull
	List<@Valid ContactDto> contacts;

	List<@Valid DishDto> dishes;

	List<@Valid TableDto> tables;

	List<PhotoDto> photos;

	public void setDetails(List<DishDto> dishes, List<TableDto> tables, List<PhotoDto> photos) {
		this.dishes = dishes;
		this.tables = tables;
		this.photos = photos;
	}
}
