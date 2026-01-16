package my.project.restaurantservice.repository;

import my.project.restaurantservice.entity.DishEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DishRepository extends JpaRepository<DishEntity, UUID> {
}
