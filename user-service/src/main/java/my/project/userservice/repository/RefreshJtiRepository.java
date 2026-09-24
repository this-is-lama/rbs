package my.project.userservice.repository;

import my.project.userservice.entity.RefreshJtiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface RefreshJtiRepository extends JpaRepository<RefreshJtiEntity, UUID> {

	@Modifying
	@Query("""
			delete from RefreshJtiEntity r
			where r.active = false
			   or r.expiresAt < :now
			""")
	int deleteAllByActiveFalseOrExpiresAtBefore(@Param("now") Instant now);

	@Modifying
	@Query("""
			update RefreshJtiEntity r
			set r.active = false
			where r.userId = :userId
			  and r.active = true
			""")
	int deactivateAllByUserId(@Param("userId") UUID userId);

	List<RefreshJtiEntity> findAllByUserIdAndActiveTrue(UUID userId);
}
