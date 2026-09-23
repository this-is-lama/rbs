package my.project.restaurantservice.internal.dto;


import java.util.List;

public record BookingSnapshotResponse(

		BookingRestaurantDto restaurant,

		BookingTableDto table,

		List<BookingDishDto> dishes

) {}
