package my.project.restaurantservice.restaurant.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.restaurant.entity.ContactEntity;
import my.project.restaurantservice.restaurant.entity.RestaurantEntity;
import my.project.restaurantservice.restaurant.entity.WeekDay;
import my.project.restaurantservice.restaurant.entity.WorkingHoursEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantRepositoryService {

	private final RestaurantRepository repository;
	private final WorkingHoursRepository workingHoursRepository;
	private final ContactRepository contactRepository;

	@Transactional(readOnly = true)
	public RestaurantEntity getRef(UUID id) {
		return repository.getReferenceById(id);
	}

	@Transactional(readOnly = true)
	public RestaurantEntity getById(UUID id) {
		return repository.findById(id).orElseThrow(() -> {
			log.warn("Ресторан не найден, restId={}", id);
			return new NotFoundException("restaurant.not-found", id);
		});
	}

	@Transactional(readOnly = true)
	public RestaurantEntity getByIdAndActiveTrue(UUID id) {
		return repository.findByIdAndActiveTrue(id).orElseThrow(() -> {
			log.warn("Ресторан не найден, restId={}", id);
			return new NotFoundException("restaurant.not-found", id);
		});
	}

	@Transactional(readOnly = true)
	public Page<RestaurantEntity> findAll(Specification<RestaurantEntity> spec, Pageable pageable) {
		return repository.findAll(spec, pageable);
	}

	@Transactional(readOnly = true)
	public List<String> findDistinctCategories() {
		return repository.findDistinctCategories();
	}

	@Transactional(readOnly = true)
	public List<WorkingHoursEntity> findAllWorkingHoursByRestaurantId(UUID restId) {
		return workingHoursRepository.findAllByRestaurantId(restId);
	}

	@Transactional(readOnly = true)
	public List<WorkingHoursEntity> findTodayWorkingHoursForRestaurants(Set<UUID> restIds, WeekDay today) {
		return workingHoursRepository.findTodayWorkingHoursForRestaurants(restIds, today);
	}

	@Transactional(readOnly = true)
	public List<ContactEntity> findAllContactsByRestaurantId(UUID restId) {
		return contactRepository.findAllByRestaurantId(restId);
	}

	@Transactional
	public RestaurantEntity save(RestaurantEntity restaurant) {
		return repository.save(restaurant);
	}

	@Transactional
	public void flush() {
		repository.flush();
	}

	@Transactional
	public void deleteById(UUID id) {
		repository.deleteById(id);
	}
}
