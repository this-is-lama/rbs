package my.project.restaurantservice.service;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.dto.table.TableDto;
import my.project.restaurantservice.entity.RestaurantEntity;
import my.project.restaurantservice.entity.TableEntity;
import my.project.restaurantservice.mapper.TableMapper;
import my.project.restaurantservice.repository.TableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TableService {

	private final TableRepository tableRepository;
	private final TableMapper tableMapper;
	private final RestaurantService restaurantService;

	@Transactional
	public UUID save(TableDto dto, UUID restId) {
		TableEntity table = tableMapper.toEntity(dto);

		RestaurantEntity restaurant = restaurantService.getRef(restId);
		restaurant.addTable(table);

		return tableRepository.save(table).getId();
	}

	@Transactional(readOnly = true)
	public TableDto findById(UUID id) {
		TableEntity table = tableRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("restaurant.table.not-found", id));

		return tableMapper.toDto(table);
	}

	@Transactional
	public TableDto update(UUID id, TableDto dto) {
		TableEntity entity = tableRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("restaurant.table.not-found", id));

		tableMapper.updateEntity(entity, dto);

		tableRepository.save(entity);

		return tableMapper.toDto(entity);
	}

	@Transactional
	public void delete(UUID id) {
		tableRepository.deleteById(id);
	}

}
