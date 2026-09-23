package my.project.userservice.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.ForbiddenException;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.repository.UserRepositoryService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityUserDetailsService implements UserDetailsService {

	private final UserRepositoryService repositoryService;

	@Override
	public UserDetails loadUserByUsername(String email) {
		UserEntity user = repositoryService.getByEmail(email);
		if (!user.isEnabled()) {
			log.warn("Пользователь отключён, email={}", email);
			throw new ForbiddenException("user.not-enabled");
		}

		List<GrantedAuthority> authorities = List.of(
				(GrantedAuthority) () -> user.getRole().name()
		);

		return User.builder()
				.username(user.getId().toString())
				.password(user.getPasswordHash())
				.authorities(authorities)
				.build();
	}
}
