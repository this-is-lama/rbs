package my.project.bookingservice;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@Slf4j
@EnableFeignClients
@SpringBootApplication(
		scanBasePackages = {
				"my.project.common",
				"my.project.bookingservice"
		}
)
public class BookingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingServiceApplication.class, args);
	}

	@PostConstruct
	public void init() {
		log.info("Сервис booking-service успешно запущен");
	}
}