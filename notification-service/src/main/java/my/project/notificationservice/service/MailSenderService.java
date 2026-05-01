package my.project.notificationservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.notificationservice.events.BookingCreatedEvent;
import my.project.notificationservice.mapper.MailContextMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailSenderService {

	private static final String LOGO_PATH = "templates/logo.svg";
	private static final String LOGO_CONTENT_ID = "rbs-logo";
	private static final String LOGO_CONTENT_TYPE = "image/svg+xml";

	@Value("${spring.mail.username}")
	private String sendFrom;

	private final JavaMailSender mailSender;
	private final SpringTemplateEngine templateEngine;
	private final MailContextMapper mapper;

	public void sendMessage(BookingCreatedEvent event) throws MessagingException {
		String sendToEmail = event.email();

		log.info("Подготовка email для отправки, bookingId={}, email={}", event.bookingId(), sendToEmail);

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		var helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

		Context context = new Context();
		context.setVariables(mapper.toContext(event));

		String html = templateEngine.process("booking-confirm", context);

		helper.setTo(sendToEmail);
		helper.setSubject("Подтверждение бронирования");
		helper.setFrom(sendFrom);
		helper.setText(html, true);

		ClassPathResource logo = new ClassPathResource(LOGO_PATH);

		if (logo.exists()) {
			helper.addInline(LOGO_CONTENT_ID, logo, LOGO_CONTENT_TYPE);
		} else {
			log.warn("Логотип письма не найден в classpath: {}. Письмо будет отправлено без inline-логотипа", LOGO_PATH);
		}

		mailSender.send(mimeMessage);

		log.info("Email успешно отправлен, bookingId={}, email={}", event.bookingId(), sendToEmail);
	}
}