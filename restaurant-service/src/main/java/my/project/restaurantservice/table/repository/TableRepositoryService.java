package my.project.restaurantservice.table.repository;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.table.entity.TableEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TableRepositoryService {

	private final TableRepository repository;

	@Transactional(readOnly = true)
	public TableEntity getByIdAndRestaurantIdAndActiveTrue(UUID id, UUID restId) {
		return repository.findByIdAndRestaurantIdAndActiveTrue(id, restId)
				.orElseThrow(() -> new NotFoundException("restaurant.table.not-found", id));
	}

	@Transactional(readOnly = true)
	public TableEntity getByIdAndRestaurantId(UUID id, UUID restId) {
		return repository.findByIdAndRestaurantId(id, restId)
				.orElseThrow(() -> new NotFoundException("restaurant.table.not-found", id));
	}

	@Transactional(readOnly = true)
	public List<TableEntity> findAllByRestaurantIdAndActiveTrueOrderByTableNumberAsc(UUID restId) {
		return repository.findAllByRestaurantIdAndActiveTrueOrderByTableNumberAsc(restId);
	}

	@Transactional(readOnly = true)
	public List<TableEntity> findAllByRestaurantIdOrderByTableNumberAsc(UUID restId) {
		return repository.findAllByRestaurantIdOrderByTableNumberAsc(restId);
	}

	@Transactional(readOnly = true)
	public List<TableEntity> findAllByRestaurantIdAndIdIn(UUID restId, Set<UUID> ids) {
		return repository.findAllByRestaurantIdAndIdIn(restId, ids);
	}

	@Transactional
	public TableEntity save(TableEntity tableEntity) {
		return repository.save(tableEntity);
	}

	@Transactional
	public void deleteByIdAndRestaurantId(UUID id, UUID restId) {
		repository.deleteByIdAndRestaurantId(id, restId);
	}

}
