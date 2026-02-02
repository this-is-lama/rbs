package my.project.userservice.service;

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
		long deleted = refreshJtiService.deleteAllDeactivatedOrExpired();
		log.debug("Deleted refresh tokens: {}", deleted);
	}
}
