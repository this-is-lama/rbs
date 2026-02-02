package my.project.common.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthUtil {

	public static UUID id(Authentication auth) {
		return UUID.fromString(auth.getName());
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
}

