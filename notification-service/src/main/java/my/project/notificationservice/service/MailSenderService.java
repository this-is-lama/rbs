package my.project.notificationservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.notificationservice.events.BookingCreatedEvent;
import my.project.notificationservice.mapper.MailContextMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


@Slf4j
@Service
@RequiredArgsConstructor
public class MailSenderService {

	@Value("${spring.mail.username}")
	private String sendFrom;

	private final JavaMailSender mailSender;
	private final SpringTemplateEngine templateEngine;
	private final MailContextMapper mapper;


	public void sendMessage(BookingCreatedEvent event) throws MessagingException {
		String sendToEmail = event.email();

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		var helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

		Context context = new Context();
		context.setVariables(mapper.toContext(event));

		String html = templateEngine.process("booking-confirm", context);
		helper.setTo(sendToEmail);
		helper.setSubject("Подтверждение бронирования");
		helper.setFrom(sendFrom);
		helper.setText(html, true);

		mailSender.send(mimeMessage);
	}
}

