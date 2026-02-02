package my.project.userservice.service;

import jakarta.annotation.Nullable;
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
		return jti;
	}


	@Transactional
	public void deactivateForRefresh(String jti, UUID userId) {
		Instant now = Instant.now();

		RefreshJtiEntity token = repository.findAllByUserIdAndActiveTrue(userId).stream()
				.filter(t -> !t.isExpired(now))
				.filter(t -> passwordEncoder.matches(jti, t.getJtiHash()))
				.findFirst()
				.orElseThrow(() -> new UnauthorizedException("user.invalid-token"));

		token.deactivate();
	}

	@Transactional
	public void deactivateForLogout(String jti, UUID userId) {
		repository.findAllByUserIdAndActiveTrue(userId).stream()
				.filter(t -> passwordEncoder.matches(jti, t.getJtiHash()))
				.findFirst()
				.ifPresent(RefreshJtiEntity::deactivate);
	}

	@Transactional
	public void deactivateAllForUser(UUID userId) {
		repository.findAllByUserIdAndActiveTrue(userId)
				.forEach(RefreshJtiEntity::deactivate);
	}


	@Transactional
	public long deleteAllDeactivatedOrExpired() {
		return repository.deleteAllByActiveFalseOrExpiresAtBefore(Instant.now());
	}

}
