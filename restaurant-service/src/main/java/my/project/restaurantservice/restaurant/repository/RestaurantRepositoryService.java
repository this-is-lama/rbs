package my.project.restaurantservice.restaurant.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.restaurant.entity.RestaurantEntity;
import my.project.restaurantservice.restaurant.entity.RestaurantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantRepositoryService {

	private final RestaurantRepository repository;

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
	public RestaurantEntity getActiveById(UUID id) {
		return repository.findByIdAndStatus(id, RestaurantStatus.ACTIVE).orElseThrow(() -> {
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

	@Transactional
	public RestaurantEntity save(RestaurantEntity restaurant) {
		return repository.save(restaurant);
	}

	@Transactional
	public void deleteById(UUID id) {
		repository.deleteById(id);
	}
}
