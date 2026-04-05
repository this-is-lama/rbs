package my.project.restaurantservice.service.table;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.dto.table.TableDto;
import my.project.restaurantservice.mapper.TableMapper;
import my.project.restaurantservice.repository.TableRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TableReadService {

	private final TableRepository repository;
	private final TableMapper mapper;

	@Cacheable(cacheNames = "publicTableById", key = "#restId + ':' + #id", sync = true)
	@Transactional(readOnly = true)
	public TableDto getPublicById(UUID restId, UUID id) {
		log.debug("Получение публичного стола, restId={}, tableId={}", restId, id);
		return repository.findByIdAndRestaurantIdAndActiveTrue(id, restId)
				.map(mapper::toDto)
				.orElseThrow(() -> new NotFoundException("restaurant.table.not-found", id));
	}

	@Cacheable(cacheNames = "privateTableById", key = "#restId + ':' + #id", sync = true)
	@Transactional(readOnly = true)
	public TableDto getPrivateById(UUID restId, UUID id) {
		log.debug("Получение полного стола, restId={}, tableId={}", restId, id);
		return repository.findByIdAndRestaurantId(id, restId)
				.map(mapper::toDto)
				.orElseThrow(() -> new NotFoundException("restaurant.table.not-found", id));
	}

	@Cacheable(cacheNames = "publicTablesByRestaurantId", key = "#restId", sync = true)
	@Transactional(readOnly = true)
	public List<TableDto> findAllPublicByRestaurantId(UUID restId) {
		log.debug("Получение публичного списка столов, restId={}", restId);
		var tables = repository.findAllByRestaurantIdAndActiveTrueOrderByTableNumberAsc(restId);
		return mapper.toDto(tables);
	}

	@Cacheable(cacheNames = "privateTablesByRestaurantId", key = "#restId", sync = true)
	@Transactional(readOnly = true)
	public List<TableDto> findAllPrivateByRestaurantId(UUID restId) {
		log.debug("Получение полного списка столов, restId={}", restId);
		var tables = repository.findAllByRestaurantIdOrderByTableNumberAsc(restId);
		return mapper.toDto(tables);
	}
}