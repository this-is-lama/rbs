package my.project.bookingservice.repository;

import my.project.bookingservice.dto.response.TableAvailabilitySlotResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.BookingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = {"restaurant", "table", "dishes"})
    Optional<BookingEntity> findById(UUID id);

    @EntityGraph(attributePaths = {"restaurant", "table", "dishes"})
    List<BookingEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    @EntityGraph(attributePaths = {"restaurant", "table", "dishes"})
    Optional<BookingEntity> findByIdAndUserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = {"restaurant", "table", "dishes"})
    List<BookingEntity> findAllByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);

    @Query("""
            select distinct b.restaurantId
            from BookingEntity b
            where b.status = :status
              and b.startAt >= :from
              and b.startAt < :to
            """)
    List<UUID> findRestaurantIdsWithBookingsBetween(BookingStatus status, Instant from, Instant to);

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

    @Query("""
            select new my.project.bookingservice.dto.response.TableAvailabilitySlotResponse(b.startAt, b.endAt)
            from BookingEntity b
            where b.restaurantId = :restId
              and b.tableId = :tableId
              and b.status = :status
              and b.startAt < :to
              and b.endAt > :from
            order by b.startAt asc
            """)
    List<TableAvailabilitySlotResponse> findTableSlotsBetween(@Param("restId") UUID restId,
                                                              @Param("tableId") UUID tableId,
                                                              @Param("status") BookingStatus status,
                                                              @Param("from") Instant from,
                                                              @Param("to") Instant to);


}
