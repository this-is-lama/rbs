package my.project.bookingservice.repository;

import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {

    List<BookingEntity> findAllByUserIdOrderByStartAtDesc(UUID userId);

    Optional<BookingEntity> findByIdAndUserId(UUID id, UUID userId);

    List<BookingEntity> findAllByRestaurantId(UUID restId);

    List<BookingEntity> findAllByRestaurantIdAndTableIdAndStatusAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
            UUID restaurantId,
            UUID tableId,
            BookingStatus status,
            Instant dayEnd,
            Instant dayStart
    );
}
