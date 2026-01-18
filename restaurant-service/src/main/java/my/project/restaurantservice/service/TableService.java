package my.project.restaurantservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.DishDto;
import my.project.restaurantservice.dto.TableDto;
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

		RestaurantEntity restaurantRef = restaurantService.getRef(restId);
		table.setRestaurant(restaurantRef);

		return tableRepository.save(table).getId();
	}

	@Transactional(readOnly = true)
	public TableDto findById(UUID id) {
		TableEntity table = tableRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(id.toString()));

		return tableMapper.toDto(table);
	}

	@Transactional
	public void delete(UUID id) {
		tableRepository.deleteById(id);
	}

}
