package my.project.restaurantservice.repository;

import my.project.restaurantservice.entity.DishPhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DishPhotoRepository extends JpaRepository<DishPhotoEntity, UUID> {
}
