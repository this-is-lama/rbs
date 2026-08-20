package my.project.bookingservice.client;

import jakarta.validation.Valid;
import my.project.bookingservice.config.FeignConfig;
import my.project.bookingservice.dto.client.BookingSnapshotRequest;
import my.project.bookingservice.dto.client.BookingSnapshotResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(
		name = "restaurant-service",
		configuration = FeignConfig.class
)
public interface RestaurantServiceClient {

	@GetMapping("/api/v1/restaurants/{restId}/manager-access")
	boolean hasManagerAccess(@PathVariable UUID restId);

	@PostMapping("/api/v1/restaurants/{restId}/booking-snapshot")
	BookingSnapshotResponse bookingSnapshot(@PathVariable UUID restId,
											@RequestBody @Valid BookingSnapshotRequest req);

}
