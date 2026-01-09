package my.project.userservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {

	private static final String ROLES_CLAIM = "roles";

	private final SecretKey secretKey;
	private final Duration lifetime;
	private final String issuer;

	public JwtUtils(
			@Value("${jwt.secret}") String secret,
			@Value("${jwt.lifetime}") Duration lifetime,
			@Value("${jwt.issuer:user-service}") String issuer
	) {
		// ожидаем Base64 секрет (лучший вариант)
		this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		this.lifetime = lifetime;
		this.issuer = issuer;
	}

	public String generateToken(UserDetails userDetails) {
		Instant now = Instant.now();
		Instant exp = now.plus(lifetime);

		List<String> roles = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.toList();

		return Jwts.builder()
				.issuer(issuer)
				.subject(userDetails.getUsername()) // сейчас email; позже можно заменить на userId
				.issuedAt(Date.from(now))
				.expiration(Date.from(exp))
				.claim(ROLES_CLAIM, roles)
				.signWith(secretKey) // JJWT сам выберет корректный HS-алгоритм по ключу
				.compact();
	}

	public boolean validateToken(String token) {
		try {
			parse(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public String getUsername(String token) {
		return parse(token).getPayload().getSubject();
	}

	public List<String> getRoles(String token) {
		Object value = parse(token).getPayload().get(ROLES_CLAIM);
		if (value instanceof List<?> list) {
			return list.stream().map(String::valueOf).toList();
		}
		return List.of();
	}

	private Jws<Claims> parse(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.requireIssuer(issuer)
				.build()
				.parseSignedClaims(token);
	}
}


