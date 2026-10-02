package my.project.restaurantservice.restaurant.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import my.project.restaurantservice.restaurant.entity.RestaurantStatus;

import java.util.UUID;

/**
 * Основные данные ресторана. Контакты и часы работы здесь не хранятся,
 * они читаются и обновляются отдельно (пакеты contact и workinghours).
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RestaurantDto {

	UUID id;

	String name;

	String category;

	String description;

	String city;

	String street;

	String house;

	RestaurantStatus status;
}
