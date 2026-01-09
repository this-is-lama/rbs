package my.project.userservice.repository;

import my.project.userservice.entity.ManagerRestaurantEntity;
import my.project.userservice.entity.ManagerRestaurantId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerRestaurantRepository extends
		JpaRepository<ManagerRestaurantEntity, ManagerRestaurantId> {
}
