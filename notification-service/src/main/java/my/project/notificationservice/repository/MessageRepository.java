package my.project.notificationservice.repository;

import my.project.notificationservice.entity.MessageEntity;
import my.project.notificationservice.entity.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {

	@Query(value = """
        select *
        from processed_messages
        where attempts < :maxAttempts
          and (
               status = 'CREATED'
               or (status = 'PROCESSING' and updated_at < (now() - (:stuckMinutes || ' minutes')::interval))
          )
        order by updated_at asc
        for update skip locked
        limit 500
        """, nativeQuery = true)
	List<MessageEntity> lockWorkBatch(@Param("maxAttempts") int maxAttempts, @Param("stuckMinutes") int stuckMinutes);


	long deleteTop500ByStatusAndUpdatedAtLessThan(MessageStatus status, Instant processedAt);


}