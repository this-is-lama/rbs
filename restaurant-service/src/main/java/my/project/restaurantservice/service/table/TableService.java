package my.project.restaurantservice.service.table;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.dto.client.BookingTableDto;
import my.project.restaurantservice.dto.table.TableDto;
import my.project.restaurantservice.entity.RestaurantEntity;
import my.project.restaurantservice.entity.TableEntity;
import my.project.restaurantservice.mapper.TableMapper;
import my.project.restaurantservice.repository.RestaurantRepository;
import my.project.restaurantservice.repository.TableRepository;
import my.project.restaurantservice.service.manager.ManagerService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TableService {

	private final TableRepository repository;
	private final RestaurantRepository restaurantRepository;
	private final TableMapper mapper;
	private final TableReadService readService;
	private final ManagerService managerService;

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicTablesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "privateTablesByRestaurantId", key = "#restId")
	})
	@Transactional
	public UUID save(TableDto dto, UUID restId, Authentication auth) {
		log.info("Создание стола, restId={}, tableNumber={}", restId, dto.tableNumber());
		managerService.checkAccess(restId, auth);

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
		managerService.checkAccess(restId, auth);

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
		managerService.checkAccess(restId, auth);

		TableEntity entity = repository.findByIdAndRestaurantId(id, restId)
				.orElseThrow(() -> new NotFoundException("restaurant.table.not-found", id));

		mapper.updateEntity(entity, dto);

		log.info("Стол успешно обновлён, restId={}, tableId={}", restId, id);
		return mapper.toDto(entity);
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
		managerService.checkAccess(restId, auth);
		repository.deleteByIdAndRestaurantId(id, restId);
		log.info("Стол успешно удалён, restId={}, tableId={}", restId, id);
	}

	@Transactional(readOnly = true)
	public TableDto findById(UUID restId, UUID id, Authentication auth) {
		log.info("Получение стола, restId={}, tableId={}", restId, id);
		return managerService.onlyPublic(restId, auth)
				? readService.getPublicById(restId, id)
				: readService.getPrivateById(restId, id);
	}

	@Transactional(readOnly = true)
	public List<TableDto> findAllByRestaurantId(UUID restId, Authentication auth) {
		log.info("Получение списка столов ресторана, restId={}", restId);
		return managerService.onlyPublic(restId, auth)
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
}