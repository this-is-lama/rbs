package my.project.restaurantservice.repository;

import my.project.restaurantservice.entity.RestaurantEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<RestaurantEntity, UUID> {

	@EntityGraph(attributePaths = {"workingHours", "contacts", "dishes"})
	Optional<RestaurantEntity> findDetailsById(UUID id);
}
