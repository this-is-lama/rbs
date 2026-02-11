package my.project.restaurantservice.dto.client;


import java.util.List;

public record BookingSnapshotResponse(

		BookingRestaurantDto restaurant,

		BookingTableDto table,

		List<BookingDishDto> dishes

) {}
