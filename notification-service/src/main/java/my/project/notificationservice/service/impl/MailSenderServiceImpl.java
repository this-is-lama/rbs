package my.project.notificationservice.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.logging.Loggable;
import my.project.notificationservice.entity.MessageType;
import my.project.notificationservice.events.BookingNotificationEvent;
import my.project.notificationservice.mapper.MailContextMapper;
import my.project.notificationservice.service.SenderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Loggable
@Slf4j
@Service
@RequiredArgsConstructor
public class MailSenderServiceImpl implements SenderService {

	private static final String LOGO_PATH = "templates/logo.svg";
	private static final String LOGO_CONTENT_ID = "rbs-logo";
	private static final String LOGO_CONTENT_TYPE = "image/svg+xml";

	@Value("${spring.mail.username}")
	private String sendFrom;

	private final JavaMailSender mailSender;
	private final SpringTemplateEngine templateEngine;
	private final MailContextMapper mapper;

	public void sendMessage(BookingNotificationEvent event) throws MessagingException {
		var message = createMessage(event);
		mailSender.send(message);
	}

	private MimeMessage createMessage(BookingNotificationEvent event) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		var helper = new MimeMessageHelper(message, true, "UTF-8");

		Context context = new Context();
		var variables = mapper.toContext(event);
		context.setVariables(variables);

		String html = templateEngine.process(templateName(event.messageType()), context);

		helper.setTo(event.email());
		helper.setSubject(subject(event.messageType()));
		helper.setFrom(sendFrom);
		helper.setText(html, true);

		ClassPathResource logo = new ClassPathResource(LOGO_PATH);

		if (logo.exists()) {
			helper.addInline(LOGO_CONTENT_ID, logo, LOGO_CONTENT_TYPE);
		} else {
			log.warn("Логотип письма не найден в classpath: {}. Письмо будет отправлено без inline-логотипа", LOGO_PATH);
		}

		return message;
	}

	private String templateName(MessageType messageType) {
		return switch (messageType) {
			case BOOKING_CREATED -> "booking-confirm";
			case BOOKING_CANCELLED -> "booking-cancelled";
		};
	}

	private String subject(MessageType messageType) {
		return switch (messageType) {
			case BOOKING_CREATED -> "Подтверждение бронирования";
			case BOOKING_CANCELLED -> "Бронирование отменено";
		};
	}
}