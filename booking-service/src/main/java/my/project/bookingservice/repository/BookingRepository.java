package my.project.bookingservice.repository;

import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {

    List<BookingEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<BookingEntity> findByIdAndUserId(UUID id, UUID userId);

    List<BookingEntity> findAllByRestaurantIdOrderByCreatedAtDesc(UUID restId);

    List<BookingEntity> findAllByRestaurantIdAndTableIdAndStatusInAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
            UUID restaurantId,
            UUID tableId,
            Collection<BookingStatus> statuses,
            Instant dayEnd,
            Instant dayStart
    );

    @Query("""
            select count(distinct b.tableId)
            from BookingEntity b
            where b.restaurantId = :restaurantId
              and b.status in :statuses
              and b.startAt < :endAt
              and b.endAt > :startAt
            """)
    long countOccupiedTables(
            @Param("restaurantId") UUID restaurantId,
            @Param("statuses") Collection<BookingStatus> statuses,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt
    );

    List<BookingEntity> findAllByRestaurantIdAndStatusAndStartAtGreaterThanEqual(
            UUID restaurantId,
            BookingStatus status,
            Instant from
    );

    List<BookingEntity> findAllByRestaurantIdAndStatusAndStartAtGreaterThanEqualAndStartAtLessThan(
            UUID restaurantId,
            BookingStatus status,
            Instant from,
            Instant to
    );

    @Query("""
            select distinct b.restaurantId
            from BookingEntity b
            where b.status = :status
              and b.startAt >= :from
              and b.startAt < :to
            """)
    List<UUID> findRestaurantIdsWithBookingsBetween(BookingStatus status, Instant from, Instant to);

    @Query("""
            select distinct b.restaurantId
            from BookingEntity b
            where b.status = :status
            group by b.restaurantId
            having count(b) >= :minBookings
            """)
    List<UUID> findRestaurantIdsWithAtLeastSuccessfulBookings(BookingStatus status, long minBookings);

    @Query("""
            select b.restaurantId
            from BookingEntity b
            where b.status = :status
              and b.startAt >= :from
              and b.startAt < :to
            group by b.restaurantId
            having count(b.id) >= :minCount
            """)
    List<UUID> findRestaurantIdsWithAtLeastSuccessfulBookingsBetween(
            @Param("status") BookingStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("minCount") long minCount
    );
}
