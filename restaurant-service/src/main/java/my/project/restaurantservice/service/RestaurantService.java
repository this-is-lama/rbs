package my.project.restaurantservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.dto.CreateRestaurantRequest;
import my.project.restaurantservice.dto.RestaurantResponse;
import my.project.restaurantservice.entity.ContactEntity;
import my.project.restaurantservice.entity.RestaurantEntity;
import my.project.restaurantservice.entity.WorkingHoursEntity;
import my.project.restaurantservice.mapper.ContactMapper;
import my.project.restaurantservice.mapper.RestaurantMapper;
import my.project.restaurantservice.mapper.WorkingHoursMapper;
import my.project.restaurantservice.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

	private static final String RESTAURANT_BUCKET = "restaurant-media";

	private final RestaurantMapper restaurantMapper;
	private final ContactMapper contactMapper;
	private final WorkingHoursMapper workingHoursMapper;

	private final RestaurantRepository repository;


	public UUID save(CreateRestaurantRequest req) {
		RestaurantEntity restaurant = restaurantMapper.toEntity(req);

		restaurant.setActive(req.isActive() != null && req.isActive());

		if (req.contacts() != null) {
			contactMapper.toEntities(req.contacts())
					.forEach(restaurant::addContact);
		}

		if (req.workingHours() != null) {
			workingHoursMapper.toEntities(req.workingHours())
					.forEach(restaurant::addWorkingHours);
		}

		return repository.save(restaurant).getId();
	}

	@Transactional(readOnly = true)
	public RestaurantResponse findById(UUID id) {
		RestaurantEntity restaurant = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Restaurant not found: " + id));

		return restaurantMapper.toResponse(restaurant);
	}

	@Transactional(readOnly = true)
	public RestaurantEntity getRef(UUID id) {
		return repository.getReferenceById(id);
	}

}
