package my.project.restaurantservice.workinghours.service.query;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.workinghours.dto.WorkingHoursDto;
import my.project.restaurantservice.workinghours.entity.WeekDay;
import my.project.restaurantservice.workinghours.mapper.WorkingHoursMapper;
import my.project.restaurantservice.workinghours.repository.WorkingHoursRepositoryService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkingHoursQueryService {

	private final WorkingHoursRepositoryService repositoryService;
	private final WorkingHoursMapper mapper;

	@Cacheable(cacheNames = "workingHoursByRestaurantId", key = "#restId", sync = true)
	public List<WorkingHoursDto> findAllByRestaurantId(UUID restId) {
		return mapper.toDto(repositoryService.findAllByRestaurantId(restId));
	}

	/**
	 * Часы работы на сегодня для набора ресторанов (для карточек в списке). Одним запросом, без кэша.
	 */
	@Transactional(readOnly = true)
	public Map<UUID, WorkingHoursDto> findTodayForRestaurants(Set<UUID> restIds, WeekDay today) {
		return repositoryService.findTodayWorkingHoursForRestaurants(restIds, today).stream()
				.collect(Collectors.toMap(
						wh -> wh.getRestaurant().getId(),
						mapper::toDto
				));
	}
}
