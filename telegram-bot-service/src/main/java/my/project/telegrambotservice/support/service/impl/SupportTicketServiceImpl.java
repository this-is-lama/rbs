package my.project.telegrambotservice.support.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.logging.Loggable;
import my.project.telegrambotservice.config.SupportProperties;
import my.project.telegrambotservice.support.entity.MessageAuthor;
import my.project.telegrambotservice.support.entity.SupportMessageEntity;
import my.project.telegrambotservice.support.entity.SupportTicketEntity;
import my.project.telegrambotservice.support.entity.TicketRating;
import my.project.telegrambotservice.support.entity.TicketStatus;
import my.project.telegrambotservice.support.repository.SupportMessageRepository;
import my.project.telegrambotservice.support.repository.SupportTicketRepository;
import my.project.telegrambotservice.support.service.NewTicket;
import my.project.telegrambotservice.support.service.SupportTicketService;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static my.project.telegrambotservice.support.service.SupportTicketService.CloseResult.Outcome.ALREADY_CLOSED;
import static my.project.telegrambotservice.support.service.SupportTicketService.CloseResult.Outcome.CLOSED;
import static my.project.telegrambotservice.support.service.SupportTicketService.CloseResult.Outcome.NOT_FOUND;

@Slf4j
@Loggable
@Service
@RequiredArgsConstructor
public class SupportTicketServiceImpl implements SupportTicketService {

	private static final Set<TicketStatus> ACTIVE = EnumSet.of(TicketStatus.OPEN, TicketStatus.ANSWERED);

	private final SupportTicketRepository ticketRepository;
	private final SupportMessageRepository messageRepository;
	private final SupportProperties properties;

	@Override
	@Transactional(readOnly = true)
	public boolean isDailyLimitReached(long chatId) {
		Instant dayAgo = Instant.now().minus(Duration.ofDays(1));
		return ticketRepository.countByChatIdAndCreatedAtAfter(chatId, dayAgo) >= properties.maxTicketsPerDay();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<SupportTicketEntity> findActiveTicket(long chatId) {
		return ticketRepository.findFirstByChatIdAndStatusInOrderByCreatedAtDesc(chatId, ACTIVE);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<SupportTicketEntity> findById(long ticketId) {
		return ticketRepository.findById(ticketId);
	}

	@Override
	@Transactional
	public SupportTicketEntity create(NewTicket data) {
		SupportTicketEntity ticket = new SupportTicketEntity();
		ticket.setChatId(data.chatId());
		ticket.setUserDisplayName(data.userDisplayName());
		ticket.setUsername(data.username());
		ticket.setCategory(data.category());
		ticket.setDescription(data.description());
		ticket.setRestaurant(data.restaurant());
		ticket.setEmail(data.email());
		ticket.setStatus(TicketStatus.OPEN);

		SupportTicketEntity saved = ticketRepository.saveAndFlush(ticket);
		log.info("Создано обращение #{}, category={}, chatId={}", saved.getId(), saved.getCategory(), saved.getChatId());
		return saved;
	}

	@Override
	@Transactional
	public void addMessage(long ticketId, MessageAuthor author, String contentType, String text,
	                       Long userMessageId, Long supportChatId, Long supportMessageId) {
		SupportTicketEntity ticket = ticketRepository.findById(ticketId)
				.orElseThrow(() -> new IllegalStateException("Обращение #" + ticketId + " не найдено"));

		SupportMessageEntity message = new SupportMessageEntity();
		message.setTicket(ticket);
		message.setAuthor(author);
		message.setContentType(contentType);
		message.setText(text);
		message.setUserMessageId(userMessageId);
		message.setSupportChatId(supportChatId);
		message.setSupportMessageId(supportMessageId);
		messageRepository.save(message);

		TicketStatus oldStatus = ticket.getStatus();
		if (author == MessageAuthor.SUPPORT) {
			ticket.markAnswered();
		} else if (ticket.getStatus() != TicketStatus.OPEN) {
			ticket.markOpen();
		}
		// updated_at двигаем и при неизменном статусе — по нему сортируется список /tickets
		ticket.setUpdatedAt(Instant.now());

		if (oldStatus != ticket.getStatus()) {
			log.info("Статус обращения #{} изменён: {} -> {}", ticketId, oldStatus, ticket.getStatus());
		}
	}

	@Override
	@Transactional
	public void linkSupportChatMessage(long ticketId, String contentType, long supportChatId, long supportMessageId) {
		SupportMessageEntity message = new SupportMessageEntity();
		message.setTicket(ticketRepository.getReferenceById(ticketId));
		message.setAuthor(MessageAuthor.SYSTEM);
		message.setContentType(contentType);
		message.setSupportChatId(supportChatId);
		message.setSupportMessageId(supportMessageId);
		messageRepository.save(message);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<SupportTicketEntity> findBySupportMessage(long supportChatId, long supportMessageId) {
		return messageRepository.findTicketBySupportMessage(supportChatId, supportMessageId);
	}

	@Override
	@Transactional
	public CloseResult close(long ticketId) {
		Optional<SupportTicketEntity> found = ticketRepository.findById(ticketId);
		if (found.isEmpty()) {
			return new CloseResult(NOT_FOUND, null);
		}

		SupportTicketEntity ticket = found.get();
		if (ticket.getStatus() == TicketStatus.CLOSED) {
			return new CloseResult(ALREADY_CLOSED, ticket);
		}

		ticket.close();
		log.info("Обращение #{} закрыто", ticketId);
		return new CloseResult(CLOSED, ticket);
	}

	@Override
	@Transactional
	public boolean rate(long ticketId, long chatId, TicketRating rating) {
		return ticketRepository.findById(ticketId)
				.filter(ticket -> ticket.getChatId() == chatId)
				.filter(ticket -> ticket.getStatus() == TicketStatus.CLOSED)
				.filter(ticket -> ticket.getRating() == null)
				.map(ticket -> {
					ticket.setRating(rating);
					log.info("Обращение #{} оценено: {}", ticketId, rating);
					return true;
				})
				.orElse(false);
	}

	@Override
	@Transactional(readOnly = true)
	public List<SupportTicketEntity> findActiveTickets(int limit) {
		return ticketRepository.findByStatusInOrderByUpdatedAtAsc(ACTIVE, Limit.of(limit));
	}
}
