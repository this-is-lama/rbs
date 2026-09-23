package my.project.restaurantservice.table.service.command;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.restaurant.entity.RestaurantEntity;
import my.project.restaurantservice.restaurant.repository.RestaurantRepositoryService;
import my.project.restaurantservice.table.dto.TableDto;
import my.project.restaurantservice.table.dto.TableLayoutItemRequest;
import my.project.restaurantservice.table.dto.TableLayoutUpdateRequest;
import my.project.restaurantservice.table.entity.TableEntity;
import my.project.restaurantservice.table.mapper.TableMapper;
import my.project.restaurantservice.table.repository.TableRepositoryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableWriteService {

	private final TableRepositoryService repositoryService;
	private final TableMapper mapper;
	private final RestaurantRepositoryService restaurantRepositoryService;

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicTablesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "privateTablesByRestaurantId", key = "#restId")
	})
	@Transactional
	public UUID save(TableDto dto, UUID restId) {
		TableEntity tableEntity = mapper.toEntity(dto);
		RestaurantEntity restaurant = restaurantRepositoryService.getRef(restId);
		restaurant.addTable(tableEntity);

		return repositoryService.save(tableEntity).getId();
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicTablesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "privateTablesByRestaurantId", key = "#restId")
	})
	@Transactional
	public List<UUID> saveAll(List<TableDto> dtos, UUID restId) {
		RestaurantEntity restaurant = restaurantRepositoryService.getRef(restId);

		List<UUID> ids = new ArrayList<>();
		dtos.stream().map(mapper::toEntity).forEach(t -> {
			restaurant.addTable(t);
			TableEntity savedEntity = repositoryService.save(t);
			ids.add(savedEntity.getId());
		});

		return ids;
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicTableById", key = "#restId + ':' + #id"),
			@CacheEvict(cacheNames = "privateTableById", key = "#restId + ':' + #id"),
			@CacheEvict(cacheNames = "publicTablesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "privateTablesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "restaurantBookingTable", key = "#restId + ':' + #id")
	})
	@Transactional
	public TableDto update(UUID restId, UUID id, TableDto dto) {
		TableEntity tableEntity = repositoryService.getByIdAndRestaurantId(id, restId);
		mapper.updateEntity(tableEntity, dto);
		TableEntity updatedEntity = repositoryService.save(tableEntity);
		return mapper.toDto(updatedEntity);
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicTableById", key = "#restId + ':' + #id"),
			@CacheEvict(cacheNames = "privateTableById", key = "#restId + ':' + #id"),
			@CacheEvict(cacheNames = "publicTablesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "privateTablesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "restaurantBookingTable", key = "#restId + ':' + #id")
	})
	@Transactional
	public void deleteByIdAndRestaurantId(UUID id, UUID restId) {
		repositoryService.deleteByIdAndRestaurantId(id, restId);
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicTableById", allEntries = true),
			@CacheEvict(cacheNames = "privateTableById", allEntries = true),
			@CacheEvict(cacheNames = "publicTablesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "privateTablesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "restaurantBookingTable", allEntries = true)
	})
	@Transactional
	public List<TableDto> updateLayout(UUID restId, TableLayoutUpdateRequest req) {
		Set<UUID> ids = req.tables().stream()
				.map(TableLayoutItemRequest::id)
				.collect(Collectors.toSet());

		List<TableEntity> tables = repositoryService.findAllByRestaurantIdAndIdIn(restId, ids);
		if (tables.size() != ids.size()) {
			Set<UUID> foundIds = tables.stream().map(TableEntity::getId).collect(Collectors.toSet());
			Set<UUID> missedIds = new HashSet<>(ids);
			missedIds.removeAll(foundIds);
			throw new NotFoundException("restaurant.table.not-found", missedIds);
		}

		Map<UUID, TableEntity> tableById = tables.stream()
				.collect(Collectors.toMap(TableEntity::getId, table -> table));

		for (TableLayoutItemRequest item : req.tables()) {
			TableEntity tableEntity = tableById.get(item.id());
			mapper.updateLayout(tableEntity, item);
		}

		return tables.stream().map(mapper::toDto).toList();
	}
}
