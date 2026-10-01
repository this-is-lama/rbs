package my.project.userservice.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.BadRequestException;
import my.project.common.exception.ConflictException;
import my.project.common.logging.Loggable;
import my.project.common.security.AuthUtil;
import my.project.common.security.UserRole;
import my.project.userservice.dto.ChangePasswordRequest;
import my.project.userservice.dto.RegistrationRequest;
import my.project.userservice.dto.UpdateUserRequest;
import my.project.userservice.dto.UserDto;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.mapper.UserMapper;
import my.project.userservice.repository.UserRepositoryService;
import my.project.userservice.service.jwt.RefreshJtiService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Loggable
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandService {

	private final UserRepositoryService repositoryService;
	private final UserMapper mapper;

	private final RefreshJtiService refreshJtiService;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public UUID save(RegistrationRequest req) {
		UserEntity user = mapper.toEntity(req, passwordEncoder);
		return repositoryService.save(user).getId();
	}

	@Transactional
	public void syncManagerRole(UUID userId, boolean hasRestaurants) {
		UserEntity user = repositoryService.getById(userId);

		if (user.getRole() == UserRole.ROLE_ADMIN) {
			log.info("Синхронизация роли пропущена: пользователь - администратор, userId={}", userId);
			return;
		}

		UserRole newRole = hasRestaurants ? UserRole.ROLE_MANAGER : UserRole.ROLE_USER;

		if (user.getRole() != newRole) {
			user.setRole(newRole);
			refreshJtiService.deactivateAllForUser(user.getId());
			repositoryService.save(user);
			log.info("Роль пользователя синхронизирована с ресторанами, userId={}, role={}", userId, newRole);
		}
	}

	@Transactional
	public UserDto update(UpdateUserRequest req, Authentication auth) {
		UserEntity user = repositoryService.getById(AuthUtil.id(auth));
		boolean emailChanged = !user.getEmail().equals(req.email());

		if (emailChanged && repositoryService.existsByEmail(req.email())) {
			log.warn("Обновление профиля отклонено: email={} уже занят", req.email());
			throw new ConflictException("user.email-already-use");
		}

		mapper.updateEntity(req, user);
		UserEntity savedUser = repositoryService.save(user);

		if (emailChanged) {
			log.info("Email пользователя был изменён, деактивируются все refresh токены, userId={}", savedUser.getId());
			refreshJtiService.deactivateAllForUser(savedUser.getId());
		}

		return mapper.toDto(savedUser);
	}

	@Transactional
	public void changePassword(ChangePasswordRequest req, Authentication auth) {
		UUID userId = AuthUtil.id(auth);
		UserEntity user = repositoryService.getById(userId);

		if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
			log.warn("Текущий пароль указан неверно, userId={}", userId);
			throw new BadRequestException("user.invalid-current-password");
		}

		if (passwordEncoder.matches(req.newPassword(), user.getPasswordHash())) {
			log.warn("Новый пароль совпадает со старым, userId={}", userId);
			throw new BadRequestException("user.new-password-must-differ");
		}

		user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
		repositoryService.save(user);

		refreshJtiService.deactivateAllForUser(user.getId());
	}
}
