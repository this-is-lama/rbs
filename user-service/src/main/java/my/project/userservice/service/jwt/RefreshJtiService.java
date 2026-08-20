package my.project.userservice.service.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.UnauthorizedException;
import my.project.userservice.entity.RefreshJtiEntity;
import my.project.userservice.repository.RefreshJtiRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshJtiService {

	@Value("${jwt.refresh-lifetime}")
	private Duration refreshLifetime;

	private final RefreshJtiRepository repository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public String save(UUID userId) {
		var jti = UUID.randomUUID().toString();
		var jtiHash = passwordEncoder.encode(jti);
		var expiresAt = Instant.now().plus(refreshLifetime);

		var refreshToken = RefreshJtiEntity.builder()
				.jtiHash(jtiHash)
				.expiresAt(expiresAt)
				.userId(userId)
				.build();

		repository.save(refreshToken);

		log.info("Сохранён новый refresh JTI для userId={}, expiresAt={}", userId, expiresAt);
		return jti;
	}

	@Transactional
	public void deactivateForRefresh(String jti, UUID userId) {
		Instant now = Instant.now();

		RefreshJtiEntity token = repository.findAllByUserIdAndActiveTrue(userId).stream()
				.filter(t -> !t.isExpired(now))
				.filter(t -> passwordEncoder.matches(jti, t.getJtiHash()))
				.findFirst()
				.orElseThrow(() -> {
					log.warn("Не удалось деактивировать refresh JTI при обновлении токена, userId={}", userId);
					return new UnauthorizedException("user.invalid-token");
				});

		token.deactivate();
		log.info("Refresh JTI деактивирован после обновления токенов, userId={}", userId);
	}

	@Transactional
	public void deactivateForLogout(String jti, UUID userId) {
		repository.findAllByUserIdAndActiveTrue(userId).stream()
				.filter(t -> passwordEncoder.matches(jti, t.getJtiHash()))
				.findFirst()
				.ifPresent(token -> {
					token.deactivate();
					log.info("Refresh JTI деактивирован при выходе из системы, userId={}", userId);
				});
	}

	@Transactional
	public void deactivateAllForUser(UUID userId) {
		repository.findAllByUserIdAndActiveTrue(userId)
				.forEach(RefreshJtiEntity::deactivate);

		log.info("Все активные refresh JTI деактивированы для userId={}", userId);
	}

	@Transactional
	public long deleteAllDeactivatedOrExpired() {
		long deleted = repository.deleteAllByActiveFalseOrExpiresAtBefore(Instant.now());
		log.debug("Удалено refresh JTI: {}", deleted);
		return deleted;
	}
}