package my.project.common.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import my.project.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Set;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthUtil {

	public static UUID id(Authentication auth) {
		return UUID.fromString(auth.getName());
	}

	public static String email(Authentication auth) {
		if (auth instanceof JwtAuthenticationToken jwtAuth) {
			return jwtAuth.getToken().getClaim(JwtClaims.EMAIL_CLAIM);
		}
		throw new UnauthorizedException("common.unauthorized");
	}

	public static String username(Authentication auth) {
		if (auth instanceof JwtAuthenticationToken jwtAuth) {
			return jwtAuth.getToken().getClaim(JwtClaims.USERNAME_CLAIM);
		}
		throw new UnauthorizedException("common.unauthorized");
	}

	public static boolean has(Authentication auth, String role) {
		return auth != null && auth.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch(role::equals);
	}

	public static boolean hasAny(Authentication auth, Set<String> roles) {
		return auth != null && auth.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch(roles::contains);
	}

	public static boolean isAdmin(Authentication auth) {
		return has(auth, "ROLE_ADMIN");
	}

	public static boolean isManager(Authentication auth) {
		return has(auth, "ROLE_MANAGER");
	}

	public static boolean isUser(Authentication auth) {
		return auth == null || has(auth, "ROLE_USER");
	}
}

