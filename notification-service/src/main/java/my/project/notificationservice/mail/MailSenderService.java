package my.project.notificationservice.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.notificationservice.dto.BookingEmailDto;
import my.project.notificationservice.mapper.BookingEmailMapper;
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
	private String from;

	private final JavaMailSender mailSender;
	private final SpringTemplateEngine templateEngine;
	private final BookingEmailMapper bookingEmailMapper;

	public void sendBookingConfirmation(String to, BookingEmailDto dto) {

		MimeMessage mimeMessage = mailSender.createMimeMessage();

		try {
			var helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			Context context = new Context();
			context.setVariables(bookingEmailMapper.toContext(dto));

			String html = templateEngine.process("booking-confirm", context);
			helper.setTo(to);
			helper.setSubject("Подтверждение бронирования");
			helper.setFrom(from);
			helper.setText(html, true);

			mailSender.send(mimeMessage);

		} catch (MessagingException e) {
			log.error("Failed to send booking email", e);
		}
	}
}

