package my.project.restaurantservice.internal.service;

import lombok.RequiredArgsConstructor;
import my.project.common.logging.Loggable;
import my.project.restaurantservice.dish.service.query.DishQueryService;
import my.project.restaurantservice.internal.dto.BookingSnapshotRequest;
import my.project.restaurantservice.internal.dto.BookingSnapshotResponse;
import my.project.restaurantservice.restaurant.service.query.RestaurantQueryService;
import my.project.restaurantservice.table.service.query.TableQueryService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Loggable
@Service
@RequiredArgsConstructor
public class BookingSnapshotDetailsService {

	private final RestaurantQueryService restaurantQueryService;
	private final DishQueryService dishQueryService;
	private final TableQueryService tableQueryService;

	public BookingSnapshotResponse bookingSnapshot(UUID restId, BookingSnapshotRequest req) {
		var restaurant = restaurantQueryService.getBookingRestaurant(restId);
		var dishes = dishQueryService.findRestaurantBookingDishes(restId, req.dishes());
		var table = tableQueryService.findRestaurantBookingTable(restId, req.tableId());

		return new BookingSnapshotResponse(restaurant, table, dishes);
	}

}
