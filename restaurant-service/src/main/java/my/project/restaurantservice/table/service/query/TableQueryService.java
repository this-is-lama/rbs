package my.project.restaurantservice.table.service.query;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.internal.dto.BookingTableDto;
import my.project.restaurantservice.table.dto.TableDto;
import my.project.restaurantservice.table.entity.TableEntity;
import my.project.restaurantservice.table.mapper.TableMapper;
import my.project.restaurantservice.table.repository.TableRepositoryService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TableQueryService {

	private final TableRepositoryService repositoryService;
	private final TableMapper mapper;

	@Cacheable(cacheNames = "publicTableById", key = "#restId + ':' + #id", sync = true)
	public TableDto getPublicById(UUID restId, UUID id) {
		TableEntity tableEntity = repositoryService.getByIdAndRestaurantIdAndActiveTrue(id, restId);
		return mapper.toDto(tableEntity);
	}

	@Cacheable(cacheNames = "privateTableById", key = "#restId + ':' + #id", sync = true)
	public TableDto getPrivateById(UUID restId, UUID id) {
		TableEntity tableEntity = repositoryService.getByIdAndRestaurantId(id, restId);
		return mapper.toDto(tableEntity);
	}

	@Cacheable(cacheNames = "publicTablesByRestaurantId", key = "#restId", sync = true)
	public List<TableDto> findAllPublicByRestaurantId(UUID restId) {
		List<TableEntity> tables = repositoryService.findAllByRestaurantIdAndActiveTrueOrderByTableNumberAsc(restId);
		return mapper.toDto(tables);
	}

	@Cacheable(cacheNames = "privateTablesByRestaurantId", key = "#restId", sync = true)
	public List<TableDto> findAllPrivateByRestaurantId(UUID restId) {
		List<TableEntity> tables = repositoryService.findAllByRestaurantIdOrderByTableNumberAsc(restId);
		return mapper.toDto(tables);
	}

	@Cacheable(cacheNames = "restaurantBookingTable", key = "#restId + ':' + #id", sync = true)
	public BookingTableDto findRestaurantBookingTable(UUID restId, UUID id) {
		TableEntity tableEntity = repositoryService.getByIdAndRestaurantIdAndActiveTrue(id, restId);
		return mapper.toBookingDto(tableEntity);
	}
}
