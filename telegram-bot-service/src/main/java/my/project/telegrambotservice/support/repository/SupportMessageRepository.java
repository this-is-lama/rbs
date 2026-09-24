package my.project.telegrambotservice.support.repository;

import my.project.telegrambotservice.support.entity.SupportMessageEntity;
import my.project.telegrambotservice.support.entity.SupportTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SupportMessageRepository extends JpaRepository<SupportMessageEntity, Long> {

	@Query("""
			select m.ticket
			from SupportMessageEntity m
			where m.supportChatId = :chatId
			  and m.supportMessageId = :messageId
			""")
	Optional<SupportTicketEntity> findTicketBySupportMessage(@Param("chatId") long chatId, @Param("messageId") long messageId);
}
