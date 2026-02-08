package my.project.restaurantservice.repository;

import my.project.restaurantservice.entity.ContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<ContactEntity, UUID> {

	List<ContactEntity> findAllByRestaurantId(UUID restId);
}
