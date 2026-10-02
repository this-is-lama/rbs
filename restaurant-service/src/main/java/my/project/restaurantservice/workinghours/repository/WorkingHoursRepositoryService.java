package my.project.restaurantservice.workinghours.repository;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.workinghours.entity.WeekDay;
import my.project.restaurantservice.workinghours.entity.WorkingHoursEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkingHoursRepositoryService {

	private final WorkingHoursRepository repository;

	@Transactional(readOnly = true)
	public List<WorkingHoursEntity> findAllByRestaurantId(UUID restId) {
		return repository.findAllByRestaurantId(restId).stream()
				.sorted(Comparator.comparing(WorkingHoursEntity::getDayOfWeek))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<WorkingHoursEntity> findTodayWorkingHoursForRestaurants(Set<UUID> restIds, WeekDay today) {
		return repository.findTodayWorkingHoursForRestaurants(restIds, today);
	}
}
