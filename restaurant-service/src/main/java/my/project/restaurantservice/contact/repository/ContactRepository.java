package my.project.restaurantservice.contact.repository;

import my.project.restaurantservice.contact.entity.ContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<ContactEntity, UUID> {

	List<ContactEntity> findAllByRestaurantIdOrderByCreatedAtAsc(UUID restId);
}
