package my.project.restaurantservice.restaurant.repository;

import my.project.restaurantservice.restaurant.entity.WorkingHoursEntity;
import my.project.restaurantservice.restaurant.entity.WeekDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface WorkingHoursRepository extends JpaRepository<WorkingHoursEntity, UUID> {

	List<WorkingHoursEntity> findAllByRestaurantId(UUID restId);

	@Query("""
        select wh
        from WorkingHoursEntity wh
        where wh.dayOfWeek = :today
          and wh.restaurant.id in :restIds
    """)
	List<WorkingHoursEntity> findTodayWorkingHoursForRestaurants(@Param("restIds") Set<UUID> restIds,
																 @Param("today") WeekDay today);

}

