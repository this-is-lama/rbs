package my.project.userservice.service;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.ConflictException;
import my.project.common.security.AuthUtil;
import my.project.common.exception.BadRequestException;
import my.project.common.exception.ForbiddenException;
import my.project.common.exception.NotFoundException;
import my.project.userservice.dto.*;
import my.project.userservice.entity.UserEntity;
import my.project.common.security.UserRole;
import my.project.userservice.mapper.UserMapper;
import my.project.userservice.repository.UserRepository;
import org.springframework.security.core.Authentication;
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

	private final UserRepository repository;
	private final UserMapper mapper;
	private final RefreshJtiService refreshJtiService;

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
	public UserDto getById(UUID id) {
		var user = findById(id);
		return mapper.toDto(user);
	}

	@Transactional(readOnly = true)
	public UserEntity findById(UUID id) {
		return repository.findById(id)
				.orElseThrow(() -> new NotFoundException("user.not-found-by-id", id));
	}

	@Transactional(readOnly = true)
	public UserEntity findByEmail(String email) {
		return repository.findByEmail(email)
				.orElseThrow(() -> new NotFoundException("user.not-found-by-email", email));
	}

	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return repository.existsByEmail(email);
	}

	@Transactional
	public UserEntity save(RegistrationRequest req) {
		UserEntity user = mapper.toEntity(req, passwordEncoder);
		return repository.save(user);
	}

	@Transactional
	public UUID changeRoleByEmail(ChangeRoleRequest req, Authentication auth) {
		UserRole newRole = req.role();
		UserEntity user = findByEmail(req.email());

		if (AuthUtil.isManager(auth) && newRole == UserRole.ROLE_ADMIN) {
			throw new ForbiddenException("user.admin-change-role-error");
		}
		if (user.getRole() == UserRole.ROLE_ADMIN) {
			throw new BadRequestException("user.admin-change-role-error");
		}
		user.setRole(newRole);
		refreshJtiService.deactivateAllForUser(user.getId());

		return repository.save(user).getId();
	}

	@Transactional
	public UserDto update(UUID userId, UpdateUserRequest req) {
		UserEntity user = findById(userId);

		boolean emailChanged = !user.getEmail().equals(req.email());

		if (emailChanged && existsByEmail(req.email())) {
			throw new ConflictException("user.email-already-use");
		}

		mapper.updateEntity(req, user);
		UserEntity savedUser = repository.save(user);

		if (emailChanged) {
			refreshJtiService.deactivateAllForUser(savedUser.getId());
		}

		return mapper.toDto(savedUser	);
	}

	@Transactional
	public void changePassword(UUID userId, ChangePasswordRequest req) {
		UserEntity user = findById(userId);

		if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
			throw new BadRequestException("user.invalid-current-password");
		}

		if (passwordEncoder.matches(req.newPassword(), user.getPasswordHash())) {
			throw new BadRequestException("user.new-password-must-differ");
		}

		user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
		repository.save(user);

		refreshJtiService.deactivateAllForUser(user.getId());
	}

}
