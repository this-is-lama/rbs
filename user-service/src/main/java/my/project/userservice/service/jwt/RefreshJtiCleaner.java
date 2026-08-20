package my.project.userservice.service.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshJtiCleaner {

	private final RefreshJtiService refreshJtiService;

	@Scheduled(fixedDelayString = "PT1H")
	public void cleanRefreshToken() {
		log.info("Запущена очистка неактивных и просроченных refresh токенов");
		long deleted = refreshJtiService.deleteAllDeactivatedOrExpired();
		log.info("Очистка refresh токенов завершена, удалено записей: {}", deleted);
	}
}