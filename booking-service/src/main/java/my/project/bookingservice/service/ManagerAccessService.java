package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.client.RestaurantServiceClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManagerAccessService {
	private final RestaurantServiceClient restaurantClient;

	@Cacheable(cacheNames = "managerAccess", key = "#managerId + ':' + #restaurantId")
	public boolean hasManagerAccess(UUID managerId, UUID restaurantId) {
		return restaurantClient.hasManagerAccess(restaurantId);
	}
}
