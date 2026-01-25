package my.project.restaurantservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.dto.RestaurantDto;
import my.project.restaurantservice.dto.RestaurantInfoDto;
import my.project.restaurantservice.entity.RestaurantEntity;
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

	private final RestaurantMapper restaurantMapper;
	private final ContactMapper contactMapper;
	private final WorkingHoursMapper workingHoursMapper;

	private final RestaurantRepository repository;

	@Transactional
	public UUID save(RestaurantDto dto) {
		RestaurantEntity restaurant = restaurantMapper.toEntity(dto);

		restaurant.setActive(dto.isActive() != null && dto.isActive());

		if (dto.contacts() != null) {
			contactMapper.toEntity(dto.contacts())
					.forEach(restaurant::addContact);
		}

		if (dto.workingHours() != null) {
			workingHoursMapper.toEntity(dto.workingHours())
					.forEach(restaurant::addWorkingHours);
		}

		return repository.save(restaurant).getId();
	}

	@Transactional(readOnly = true)
	public RestaurantDto findById(UUID id) {
		RestaurantEntity restaurant = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Restaurant not found: " + id));

		return restaurantMapper.toDto(restaurant);
	}

	@Transactional(readOnly = true)
	public List<RestaurantInfoDto> findAll() {
		List<RestaurantEntity> restaurants = repository.findAll();
		return restaurantMapper.toInfoDto(restaurants);
	}

	@Transactional
	public void delete(UUID id) {
		repository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public RestaurantEntity getRef(UUID id) {
		return repository.getReferenceById(id);
	}

}
