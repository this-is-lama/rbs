package my.project.restaurantservice.workinghours.service.command;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.restaurant.repository.RestaurantRepositoryService;
import my.project.restaurantservice.workinghours.dto.WorkingHoursDto;
import my.project.restaurantservice.workinghours.entity.WeekDay;
import my.project.restaurantservice.workinghours.mapper.WorkingHoursMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkingHoursWriteService {

	private final RestaurantRepositoryService restaurantRepositoryService;
	private final WorkingHoursMapper mapper;

	/**
	 * Заменяет расписание: существующие дни обновляются, отсутствующие в запросе удаляются, новые добавляются.
	 */
	@CacheEvict(cacheNames = "workingHoursByRestaurantId", key = "#restId")
	@Transactional
	public void update(UUID restId, List<WorkingHoursDto> dtos) {
		var restaurant = restaurantRepositoryService.getById(restId);
		Map<WeekDay, WorkingHoursDto> byDay = dtos.stream()
				.collect(Collectors.toMap(WorkingHoursDto::dayOfWeek, Function.identity()));

		for (var wh : new ArrayList<>(restaurant.getWorkingHours())) {
			var dto = byDay.remove(wh.getDayOfWeek());
			if (dto == null) {
				restaurant.removeWorkingHours(wh);
			} else {
				mapper.updateEntity(wh, dto);
			}
		}

		byDay.values().stream()
				.map(mapper::toEntity)
				.forEach(restaurant::addWorkingHours);
	}
}
