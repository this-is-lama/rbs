package my.project.restaurantservice.repository;

import my.project.restaurantservice.entity.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TableRepository extends JpaRepository<TableEntity, UUID> {

	Optional<TableEntity> findByIdAndRestaurantId(UUID id, UUID restId);

	void deleteByIdAndRestaurantId(UUID id, UUID restId);

}
