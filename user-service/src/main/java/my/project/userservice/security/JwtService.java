package my.project.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import my.project.userservice.entity.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtService {

	private static final String ROLES_CLAIM = "roles";
	private static final String EMAIL_CLAIM = "email";
	private static final String TOKEN_TYPE = "token_type";

	private static final String ACCESS_TOKEN = "access_token";
	private static final String REFRESH_TOKEN = "refresh_token";

	private final SecretKey secretKey;
	private final SecretKey refreshSecretKey;

	private final Duration lifetime;
	private final Duration refreshLifetime;
	private final String issuer;

	public JwtService(@Value("${jwt.secret}") String secret,
					  @Value("${jwt.refresh-secret}") String refreshSecret,
					  @Value("${jwt.lifetime}") Duration lifetime,
					  @Value("${jwt.refresh-lifetime}") Duration refreshLifetime,
					  @Value("${jwt.issuer:user-service}") String issuer) {
		this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		this.refreshSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
		this.lifetime = lifetime;
		this.refreshLifetime = refreshLifetime;
		this.issuer = issuer;
	}

	public String generateToken(UserEntity user) {
		Instant now = Instant.now();
		Instant exp = now.plus(lifetime);

		List<String> roles = List.of(user.getRole().name());

		return Jwts.builder()
				.issuer(issuer)
				.subject(user.getId().toString())
				.issuedAt(Date.from(now))
				.expiration(Date.from(exp))
				.claim(ROLES_CLAIM, roles)
				.claim(EMAIL_CLAIM, user.getEmail())
				.claim(TOKEN_TYPE, ACCESS_TOKEN)
				.signWith(secretKey)
				.compact();
	}

	public String generateRefreshToken(UserEntity user) {
		Instant now = Instant.now();
		Instant exp = now.plus(refreshLifetime);

		return Jwts.builder()
				.issuer(issuer)
				.subject(user.getId().toString())
				.issuedAt(Date.from(now))
				.expiration(Date.from(exp))
				.claim(TOKEN_TYPE, REFRESH_TOKEN)
				.signWith(refreshSecretKey)
				.compact();
	}


	public boolean validateToken(String token) {
		try {
			parseToken(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public boolean validateRefreshToken(String token) {
		try {
			parseRefreshToken(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public UUID getUserIdFromToken(String token) {
		return UUID.fromString(parseToken(token).getPayload().getSubject());
	}

	public UUID getUserIdFromRefreshToken(String token) {
		return UUID.fromString(parseRefreshToken(token).getPayload().getSubject());
	}

	public List<String> getRoles(String token) {
		Object value = parseToken(token).getPayload().get(ROLES_CLAIM);
		if (value instanceof List<?> list) {
			return list.stream().map(String::valueOf).toList();
		}
		return List.of();
	}



	private Jws<Claims> parseToken(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.requireIssuer(issuer)
				.require(TOKEN_TYPE, ACCESS_TOKEN)
				.build()
				.parseSignedClaims(token);
	}

	private Jws<Claims> parseRefreshToken(String token) {
		return Jwts.parser()
				.verifyWith(refreshSecretKey)
				.requireIssuer(issuer)
				.require(TOKEN_TYPE, REFRESH_TOKEN)
				.build()
				.parseSignedClaims(token);
	}
}


