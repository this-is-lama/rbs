package my.project.restaurantservice.dto.dish;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import my.project.restaurantservice.dto.photo.PhotoDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DishDto {

	UUID id;

	@NotBlank
	@Size(max = 255)
	String name;

	@NotBlank
	@Size(max = 100)
	String category;

	@Size(max = 2000)
	String description;

	@NotNull
	@Positive
	BigDecimal price;

	@NotNull
	@Positive
	Integer weight;

	@NotNull
	Boolean available;

	List<PhotoDto> photos;
}
