package my.project.bookingservice.client;

import my.project.bookingservice.config.FeignConfig;
import my.project.bookingservice.dto.DishDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(
		name = "restaurant-service",
		configuration = FeignConfig.class
)
public interface RestaurantServiceClient {

	@PostMapping("/api/v1/restaurants/{restId}/dishes/ids")
	List<DishDto> findAllByIds(@PathVariable UUID restId, @RequestBody List<UUID> ids);

	@GetMapping("/api/v1/restaurants/{restId}/manager-access")
	boolean managerAccess(@PathVariable UUID restId);

}
