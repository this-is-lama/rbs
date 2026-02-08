package my.project.restaurantservice.service;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.NotFoundException;
import my.project.common.security.AuthUtil;
import my.project.restaurantservice.dto.table.TableDto;
import my.project.restaurantservice.entity.RestaurantEntity;
import my.project.restaurantservice.entity.TableEntity;
import my.project.restaurantservice.mapper.TableMapper;
import my.project.restaurantservice.repository.TableRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TableService {

	private final TableRepository repository;
	private final TableMapper tableMapper;
	private final RestaurantService restaurantService;
	private final ManagerService managerService;

	@Transactional
	public UUID save(TableDto dto, UUID restId, Authentication auth) {
		managerService.checkAccess(restId, auth);
		TableEntity table = tableMapper.toEntity(dto);

		RestaurantEntity restaurant = restaurantService.getRef(restId);
		restaurant.addTable(table);

		return repository.save(table).getId();
	}

	@Transactional
	public TableDto update(UUID restId, UUID id, TableDto dto, Authentication auth) {
		managerService.checkAccess(restId, auth);
		TableEntity entity = repository.findByIdAndRestaurantId(id, restId)
				.orElseThrow(() -> new NotFoundException("restaurant.table.not-found", id));

		tableMapper.updateEntity(entity, dto);

		return tableMapper.toDto(entity);
	}

	@Transactional
	public void delete(UUID restId, UUID id, Authentication auth) {
		managerService.checkAccess(restId, auth);
		repository.deleteByIdAndRestaurantId(id, restId);
	}

	@Transactional(readOnly = true)
	public TableDto findById(UUID restId, UUID id, Authentication auth) {
		var table = getById(restId, id, auth);
		return tableMapper.toDto(table);
	}

	@Transactional(readOnly = true)
	public TableEntity getById(UUID restId, UUID id, Authentication auth) {
		var userId = AuthUtil.id(auth);

		Optional<TableEntity> table;
		if (AuthUtil.isUser(auth) || (AuthUtil.isManager(auth) && !managerService.managerHasAccess(restId, userId))) {
			table = repository.findByIdAndRestaurantIdAndActiveTrue(id, restId);
		} else {
			table = repository.findByIdAndRestaurantId(id, restId);
		}
		return table.orElseThrow(() -> new NotFoundException("restaurant.table.not-found", id));
	}


	@Transactional(readOnly = true)
	public List<TableEntity> findAllByRestaurantId(UUID restId, Authentication auth) {
		var userId = AuthUtil.id(auth);
		if (AuthUtil.isUser(auth) || (AuthUtil.isManager(auth) && !managerService.managerHasAccess(restId, userId))) {
			return repository.findAllByRestaurantIdAndActiveTrueOrderByTableNumberAsc(restId);
		}
		return repository.findAllByRestaurantIdOrderByTableNumberAsc(restId);
	}

}
