package my.project.userservice.service;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.ForbiddenException;
import my.project.common.exception.NotFoundException;
import my.project.userservice.dto.register.RegistrationRequest;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.repository.UserRepository;
import my.project.userservice.mapper.UserMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) {
		UserEntity user = findByEmail(email);
		if (!user.isEnabled()) {
			throw new ForbiddenException("user.not-enabled");
		}
		return new User(
				user.getEmail(),
				user.getPasswordHash(),
				List.of(new SimpleGrantedAuthority(user.getRole().name()))
		);
	}

	@Transactional(readOnly = true)
	public UserEntity findById(UUID id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("user.not-found-by-id", id));
	}

	@Transactional(readOnly = true)
	public UserEntity findByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new NotFoundException("user.not-found-by-email", email));
	}

	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	@Transactional
	public UserEntity save(RegistrationRequest req) {
		UserEntity user = userMapper.toEntity(req, passwordEncoder);
		return userRepository.save(user);
	}
}
