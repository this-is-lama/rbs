package my.project.restaurantservice.repository;

import my.project.restaurantservice.entity.DishEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface DishRepository extends JpaRepository<DishEntity, UUID> {


	Optional<DishEntity> findByIdAndRestaurantId(UUID id, UUID restId);
	Optional<DishEntity> findByIdAndRestaurantIdAndAvailableTrue(UUID id, UUID restId);

	List<DishEntity> findAllByRestaurantIdOrderByNameAsc(UUID restId);
	List<DishEntity> findAllByRestaurantIdAndAvailableTrueOrderByNameAsc(UUID restId);


	List<DishEntity> findAllByRestaurantIdAndAvailableTrueAndIdIn(UUID restId, Set<UUID> ids);


	@EntityGraph(attributePaths = {"restaurant"})
	Optional<DishEntity> findWithRestaurantById(UUID id);


	void deleteByIdAndRestaurantId(UUID id, UUID restId);
}