package my.project.userservice.service.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.exception.InvalidTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static my.project.common.security.JwtClaims.*;

@Slf4j
@Service
public class JwtService {

	private final SecretKey accessSecretKey;
	private final SecretKey refreshSecretKey;

	private final Duration accessLifetime;
	private final Duration refreshLifetime;
	private final String issuer;

	public JwtService(@Value("${jwt.access-secret}") String accessSecret,
					  @Value("${jwt.refresh-secret}") String refreshSecret,
					  @Value("${jwt.access-lifetime}") Duration accessLifetime,
					  @Value("${jwt.refresh-lifetime}") Duration refreshLifetime,
					  @Value("${jwt.issuer:user-service}") String issuer) {
		this.accessSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
		this.refreshSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
		this.accessLifetime = accessLifetime;
		this.refreshLifetime = refreshLifetime;
		this.issuer = issuer;

		log.info("JwtService инициализирован, issuer={}, accessLifetime={}, refreshLifetime={}",
				issuer, accessLifetime, refreshLifetime);
	}

	public String generateAccessToken(UserEntity user) {
		Instant now = Instant.now();
		Instant exp = now.plus(accessLifetime);

		List<String> roles = List.of(user.getRole().name());

		log.debug("Генерация access token для userId={}, email={}", user.getId(), user.getEmail());

		return Jwts.builder()
				.issuer(issuer)
				.subject(user.getId().toString())
				.issuedAt(Date.from(now))
				.expiration(Date.from(exp))
				.claim(ROLES_CLAIM, roles)
				.claim(EMAIL_CLAIM, user.getEmail())
				.claim(USERNAME_CLAIM, user.getSurname() + " " + user.getName())
				.claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN)
				.signWith(accessSecretKey, SignatureAlgorithm.HS256)
				.compact();
	}

	public String generateRefreshToken(UserEntity user, String jti) {
		Instant now = Instant.now();
		Instant exp = now.plus(refreshLifetime);

		log.debug("Генерация refresh token для userId={}", user.getId());

		return Jwts.builder()
				.issuer(issuer)
				.subject(user.getId().toString())
				.issuedAt(Date.from(now))
				.expiration(Date.from(exp))
				.claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN)
				.claim(JTI_CLAIM, jti)
				.signWith(refreshSecretKey, SignatureAlgorithm.HS256)
				.compact();
	}

	public void validateRefreshToken(String token) {
		try {
			parseRefreshToken(token);
			log.debug("Refresh token успешно прошёл валидацию");
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("Ошибка валидации refresh token");
			throw new InvalidTokenException("user.invalid-token");
		}
	}

	public UUID getUserIdFromRefreshToken(String token) {
		UUID userId = UUID.fromString(parseRefreshToken(token).getPayload().getSubject());
		log.debug("Из refresh token извлечён userId={}", userId);
		return userId;
	}

	public String getJtiClaimFromRefreshToken(String token) {
		String jti = parseRefreshToken(token).getPayload().get(JTI_CLAIM).toString();
		log.debug("Из refresh token извлечён jti");
		return jti;
	}

	private Jws<Claims> parseRefreshToken(String token) {
		return Jwts.parser()
				.verifyWith(refreshSecretKey)
				.requireIssuer(issuer)
				.require(TOKEN_TYPE_CLAIM, REFRESH_TOKEN)
				.build()
				.parseSignedClaims(token);
	}
}