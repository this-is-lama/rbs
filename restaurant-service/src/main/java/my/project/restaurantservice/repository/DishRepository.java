package my.project.restaurantservice.repository;

import my.project.restaurantservice.entity.DishEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DishRepository extends JpaRepository<DishEntity, UUID> {

	Optional<DishEntity> findByIdAndRestaurantId(UUID id, UUID restId);

	Optional<DishEntity> findByIdAndRestaurantIdAndAvailableTrueOrderByNameAsc(UUID id, UUID restId);

	List<DishEntity> findAllByRestaurantIdAndAvailableTrueOrderByNameAsc(UUID restId);

	Optional<List<DishEntity>> findAllByRestaurantIdAndAvailableTrueAndIdIn(UUID restId, Collection<UUID> ids);

	@EntityGraph(attributePaths = {"restaurant"})
	Optional<DishEntity> findWithRestaurantById(UUID id);

	void deleteByIdAndRestaurantId(UUID id, UUID restId);
}