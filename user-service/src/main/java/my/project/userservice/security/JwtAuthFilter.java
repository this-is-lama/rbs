package my.project.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import my.project.userservice.util.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtUtils jwtUtils;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/auth/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			filterChain.doFilter(request, response);
			return;
		}

		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = header.substring(7);

		if (!jwtUtils.validateToken(token)) {
			filterChain.doFilter(request, response);
			return;
		}

		String username = jwtUtils.getUsername(token);
		var authorities = jwtUtils.getRoles(token).stream()
				.map(SimpleGrantedAuthority::new)
				.toList();

		var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
		SecurityContextHolder.getContext().setAuthentication(auth);
		filterChain.doFilter(request, response);
	}
}

