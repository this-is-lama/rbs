package my.project.restaurantservice.service.table;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.dto.client.BookingTableDto;
import my.project.restaurantservice.dto.table.TableDto;
import my.project.restaurantservice.dto.table.TableLayoutItemRequest;
import my.project.restaurantservice.dto.table.TableLayoutUpdateRequest;
import my.project.restaurantservice.entity.RestaurantEntity;
import my.project.restaurantservice.entity.TableEntity;
import my.project.restaurantservice.mapper.TableMapper;
import my.project.restaurantservice.repository.RestaurantRepository;
import my.project.restaurantservice.repository.TableRepository;
import my.project.restaurantservice.service.manager.ManagerAccessService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TableService {

	private final TableRepository repository;
	private final RestaurantRepository restaurantRepository;
	private final TableMapper mapper;
	private final TableReadService readService;
	private final ManagerAccessService managerAccessService;

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicTablesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "privateTablesByRestaurantId", key = "#restId")
	})
	@Transactional
	public UUID save(TableDto dto, UUID restId, Authentication auth) {
		log.info("Создание стола, restId={}, tableNumber={}", restId, dto.tableNumber());
		managerAccessService.checkAccess(restId, auth);

		TableEntity table = mapper.toEntity(dto);
		RestaurantEntity restaurant = restaurantRepository.getReferenceById(restId);
		restaurant.addTable(table);

		UUID id = repository.save(table).getId();
		log.info("Стол успешно создан, restId={}, tableId={}", restId, id);
		return id;
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicTablesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "privateTablesByRestaurantId", key = "#restId")
	})
	@Transactional
	public List<UUID> saveAll(List<TableDto> dtos, UUID restId, Authentication auth) {
		log.info("Массовое создание столов, restId={}, count={}", restId, dtos.size());
		managerAccessService.checkAccess(restId, auth);

		RestaurantEntity restaurant = restaurantRepository.getReferenceById(restId);
		List<UUID> ids = new ArrayList<>();

		for (TableDto dto : dtos) {
			TableEntity table = mapper.toEntity(dto);
			restaurant.addTable(table);
			ids.add(repository.save(table).getId());
		}

		log.info("Массовое создание столов завершено, restId={}, createdCount={}", restId, ids.size());
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
	public TableDto update(UUID restId, UUID id, TableDto dto, Authentication auth) {
		log.info("Обновление стола, restId={}, tableId={}", restId, id);
		managerAccessService.checkAccess(restId, auth);

		TableEntity entity = repository.findByIdAndRestaurantId(id, restId)
				.orElseThrow(() -> new NotFoundException("restaurant.table.not-found", id));

		mapper.updateEntity(entity, dto);

		log.info("Стол успешно обновлён, restId={}, tableId={}", restId, id);
		return mapper.toDto(entity);
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicTableById", allEntries = true),
			@CacheEvict(cacheNames = "privateTableById", allEntries = true),
			@CacheEvict(cacheNames = "publicTablesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "privateTablesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "restaurantBookingTable", allEntries = true)
	})
	@Transactional
	public List<TableDto> updateLayout(UUID restId, TableLayoutUpdateRequest req, Authentication auth) {
		log.info("Обновление layout столов, restId={}, count={}", restId, req.tables().size());
		managerAccessService.checkAccess(restId, auth);

		Set<UUID> ids = req.tables().stream()
				.map(TableLayoutItemRequest::id)
				.collect(Collectors.toSet());

		List<TableEntity> tables = repository.findAllByRestaurantIdAndIdIn(restId, ids);
		if (tables.size() != ids.size()) {
			Set<UUID> foundIds = tables.stream().map(TableEntity::getId).collect(Collectors.toSet());
			Set<UUID> missedIds = new HashSet<>(ids);
			missedIds.removeAll(foundIds);
			throw new NotFoundException("restaurant.table.not-found", missedIds);
		}

		Map<UUID, TableEntity> tableById = tables.stream()
				.collect(Collectors.toMap(TableEntity::getId, table -> table));

		for (TableLayoutItemRequest item : req.tables()) {
			TableEntity table = tableById.get(item.id());
			table.setPositionX(item.positionX());
			table.setPositionY(item.positionY());
			table.setMarkerSize(item.markerSize());
		}

		log.info("Layout столов успешно обновлён, restId={}", restId);
		return tables.stream()
				.sorted(Comparator.comparingInt(TableEntity::getTableNumber))
				.map(mapper::toDto)
				.toList();
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicTableById", key = "#restId + ':' + #id"),
			@CacheEvict(cacheNames = "privateTableById", key = "#restId + ':' + #id"),
			@CacheEvict(cacheNames = "publicTablesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "privateTablesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "restaurantBookingTable", key = "#restId + ':' + #id")
	})
	@Transactional
	public void delete(UUID restId, UUID id, Authentication auth) {
		log.info("Удаление стола, restId={}, tableId={}", restId, id);
		managerAccessService.checkAccess(restId, auth);
		repository.deleteByIdAndRestaurantId(id, restId);
		log.info("Стол успешно удалён, restId={}, tableId={}", restId, id);
	}

	@Transactional(readOnly = true)
	public TableDto findById(UUID restId, UUID id, Authentication auth) {
		log.info("Получение стола, restId={}, tableId={}", restId, id);
		return managerAccessService.onlyPublic(restId, auth)
				? readService.getPublicById(restId, id)
				: readService.getPrivateById(restId, id);
	}

	@Transactional(readOnly = true)
	public List<TableDto> findAllByRestaurantId(UUID restId, Authentication auth) {
		log.info("Получение списка столов ресторана, restId={}", restId);
		return managerAccessService.onlyPublic(restId, auth)
				? readService.findAllPublicByRestaurantId(restId)
				: readService.findAllPrivateByRestaurantId(restId);
	}

	@Cacheable(cacheNames = "restaurantBookingTable", key = "#restId + ':' + #id", sync = true)
	@Transactional(readOnly = true)
	public BookingTableDto findRestaurantBookingTable(UUID restId, UUID id) {
		log.debug("Получение стола для snapshot бронирования, restId={}, tableId={}", restId, id);

		var table = repository.findByIdAndRestaurantIdAndActiveTrue(id, restId)
				.orElseThrow(() -> new NotFoundException("restaurant.table.not-found", id));

		return mapper.toBookingDto(table);
	}

	@Transactional(readOnly = true)
	public long countActiveRestaurantTables(UUID restId) {
		return repository.countByRestaurantIdAndActiveTrue(restId);
	}
}
