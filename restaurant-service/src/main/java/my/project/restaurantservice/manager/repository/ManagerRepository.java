package my.project.restaurantservice.manager.repository;

import my.project.restaurantservice.manager.entity.ManagerEntity;
import my.project.restaurantservice.manager.entity.ManagerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ManagerRepository extends JpaRepository<ManagerEntity, ManagerId> {

    boolean existsByIdRestaurantIdAndIdManagerId(UUID restId, UUID managerId);

    List<ManagerEntity> findAllByIdRestaurantIdOrderByCreatedAtAsc(UUID restId);

    long countByIdManagerId(UUID managerId);

    void deleteByIdRestaurantIdAndIdManagerId(UUID restId, UUID managerId);
}