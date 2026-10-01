package my.project.restaurantservice.internal.client;

import my.project.restaurantservice.config.FeignConfig;
import my.project.restaurantservice.internal.dto.BookingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(
		name = "booking-service",
		configuration = FeignConfig.class
)
public interface BookingServiceClient {

	@GetMapping("/api/v1/bookings/manager/restaurants/{restId}")
	List<BookingDto> getRestaurantBookings(@PathVariable("restId") UUID restId);
}
