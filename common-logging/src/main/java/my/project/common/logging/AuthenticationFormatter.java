package my.project.common.logging;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

final class AuthenticationFormatter {

	private AuthenticationFormatter() {
	}

	static String format(Object value) {
		Authentication auth = (Authentication) value;
		return "Authentication[id=" + auth.getName()
				+ ", roles=" + AuthorityUtils.authorityListToSet(auth.getAuthorities()) + "]";
	}
}
