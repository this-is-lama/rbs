package my.project.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.UnauthorizedException;
import my.project.userservice.entity.RefreshTokenEntity;
import my.project.userservice.repository.RefreshTokenRepository;
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
public class RefreshTokenService {

	@Value("${jwt.refresh-lifetime}")
	Duration refreshLifetime;

	private final RefreshTokenRepository repository;

	private final PasswordEncoder passwordEncoder;

	@Transactional
	public String createRefreshJti(UUID userId) {
		var jti = UUID.randomUUID().toString();
		var jtiHash = passwordEncoder.encode(jti);

		var expiresAt = Instant.now().plus(refreshLifetime);

		var refreshToken = RefreshTokenEntity.builder()
				.jtiHash(jtiHash)
				.expiresAt(expiresAt)
				.userId(userId)
				.build();

		repository.save(refreshToken);
		return jti;
	}


	@Transactional
	public void deactivateRefreshTokenForRefresh(String jti, UUID userId) {
		Instant now = Instant.now();

		RefreshTokenEntity token = repository.findAllByUserIdAndActiveTrue(userId).stream()
				.filter(t -> !t.isExpired(now))
				.filter(t -> passwordEncoder.matches(jti, t.getJtiHash()))
				.findFirst()
				.orElseThrow(() -> new UnauthorizedException("user.invalid-token"));

		token.deactivate();
	}

	@Transactional
	public void deactivateRefreshTokenForLogout(String jti, UUID userId) {
		repository.findAllByUserIdAndActiveTrue(userId).stream()
				.filter(t -> passwordEncoder.matches(jti, t.getJtiHash()))
				.findFirst()
				.ifPresent(RefreshTokenEntity::deactivate);
	}

	@Transactional
	public long deleteAllDeactivatedOrExpired() {
		return repository.deleteAllByActiveFalseOrExpiresAtBefore(Instant.now());
	}

}
