package my.project.bookingservice.repository;

import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.BookingStatus;
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


	@Query("""
    select (count(b) > 0)
    from BookingEntity b
    join b.table t
    where t.tableId = :tableId
      and b.status = :status
      and b.startAt < :endAt
      and b.endAt > :startAt
    """)
	boolean existsOverlapping(@Param("tableId") UUID tableId,
							  @Param("status") BookingStatus status,
							  @Param("startAt") Instant startAt,
							  @Param("endAt") Instant endAt
	);

	List<BookingEntity> findAllByUserIdOrderByStartAtDesc(UUID userId);

	Optional<BookingEntity> findByIdAndUserId(UUID id, UUID userId);

	List<BookingEntity> findAllByRestaurantId(UUID restId);

}
