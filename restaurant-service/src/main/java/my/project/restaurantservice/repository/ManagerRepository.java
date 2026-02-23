package my.project.restaurantservice.repository;

import my.project.restaurantservice.entity.ManagerEntity;
import my.project.restaurantservice.entity.ManagerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ManagerRepository extends JpaRepository<ManagerEntity, ManagerId> {

    boolean existsByIdRestaurantIdAndIdManagerId(UUID restId, UUID managerId);

}
