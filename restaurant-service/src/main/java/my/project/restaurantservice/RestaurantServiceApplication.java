package my.project.restaurantservice;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableCaching
@EnableFeignClients
@EnableScheduling
@SpringBootApplication(
		scanBasePackages = {
				"my.project.restaurantservice",
				"my.project.common"
		}
)
public class RestaurantServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestaurantServiceApplication.class, args);
	}

	@PostConstruct
	public void init() {
		log.info("Сервис restaurant-service успешно запущен");
	}
}
