package my.project.restaurantservice.service.restaurant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.dto.restaurant.RestaurantDto;
import my.project.restaurantservice.mapper.RestaurantMapper;
import my.project.restaurantservice.repository.ContactRepository;
import my.project.restaurantservice.repository.RestaurantRepository;
import my.project.restaurantservice.repository.WorkingHoursRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantReadService {

	private final RestaurantRepository repository;
	private final RestaurantMapper mapper;
	private final WorkingHoursRepository workingHoursRepository;
	private final ContactRepository contactRepository;

	@Cacheable(cacheNames = "publicRestaurantById", key = "#id", sync = true)
	@Transactional(readOnly = true)
	public RestaurantDto getPublicById(UUID id) {
		log.debug("Получение публичной информации о ресторане, restId={}", id);

		var restaurant = repository.findByIdAndActiveTrue(id)
				.orElseThrow(() -> {
					log.warn("Активный ресторан не найден, restId={}", id);
					return new NotFoundException("restaurant.not-found", id);
				});

		var wh = workingHoursRepository.findAllByRestaurantId(id);
		var contacts = contactRepository.findAllByRestaurantId(id);
		return mapper.toDto(restaurant, wh, contacts);
	}

	@Cacheable(cacheNames = "privateRestaurantById", key = "#id", sync = true)
	@Transactional(readOnly = true)
	public RestaurantDto getPrivateById(UUID id) {
		log.debug("Получение полной информации о ресторане, restId={}", id);

		var restaurant = repository.findById(id)
				.orElseThrow(() -> {
					log.warn("Ресторан не найден, restId={}", id);
					return new NotFoundException("restaurant.not-found", id);
				});

		var wh = workingHoursRepository.findAllByRestaurantId(id);
		var contacts = contactRepository.findAllByRestaurantId(id);
		return mapper.toDto(restaurant, wh, contacts);
	}
}