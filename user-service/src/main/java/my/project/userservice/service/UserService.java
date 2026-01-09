package my.project.userservice.service;

import lombok.RequiredArgsConstructor;
import my.project.userservice.dto.RegistrationRequest;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.repository.UserRepository;
import my.project.userservice.util.UserMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity user = findByEmail(email).orElseThrow(() ->
				new UsernameNotFoundException("Пользователь '%s' не найден".formatted(email))
		);
		if (!user.isEnabled()) {
			throw new UsernameNotFoundException("Пользователь отключён");
		}
		return new User(
				user.getEmail(),
				user.getPasswordHash(),
				List.of(new SimpleGrantedAuthority(user.getRole().name()))
		);
	}

	@Transactional(readOnly = true)
	public Optional<UserEntity> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Transactional
	public UserEntity save(RegistrationRequest req) {
		UserEntity user = userMapper.toEntity(req, passwordEncoder);
		return userRepository.save(user);
	}
}
