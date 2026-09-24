package my.project.telegrambotservice.support.repository;

import my.project.telegrambotservice.support.entity.SupportTicketEntity;
import my.project.telegrambotservice.support.entity.TicketStatus;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SupportTicketRepository extends JpaRepository<SupportTicketEntity, Long> {

	Optional<SupportTicketEntity> findFirstByChatIdAndStatusInOrderByCreatedAtDesc(long chatId, Collection<TicketStatus> statuses);

	long countByChatIdAndCreatedAtAfter(long chatId, Instant after);

	List<SupportTicketEntity> findByStatusInOrderByUpdatedAtAsc(Collection<TicketStatus> statuses, Limit limit);
}
