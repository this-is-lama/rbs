package my.project.userservice.repository;

import my.project.userservice.entity.ManagerRestaurantEntity;
import my.project.userservice.entity.ManagerRestaurantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagerRestaurantRepository extends
		JpaRepository<ManagerRestaurantEntity, ManagerRestaurantId> {
}
