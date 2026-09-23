package my.project.userservice.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.BadRequestException;
import my.project.common.exception.ConflictException;
import my.project.common.exception.ForbiddenException;
import my.project.common.logging.Loggable;
import my.project.common.security.AuthUtil;
import my.project.common.security.UserRole;
import my.project.userservice.dto.ChangePasswordRequest;
import my.project.userservice.dto.ChangeRoleByIdRequest;
import my.project.userservice.dto.RegistrationRequest;
import my.project.userservice.dto.UpdateUserRequest;
import my.project.userservice.dto.UserDto;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.mapper.UserMapper;
import my.project.userservice.repository.UserRepositoryService;
import my.project.userservice.service.jwt.RefreshJtiService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
	public UUID changeRoleById(ChangeRoleByIdRequest req, Authentication auth) {
		UUID targetUserId = req.userId();
		UserRole newRole = req.role();

		UserEntity targetUser = repositoryService.getById(targetUserId);

		if (targetUser.getRole() == UserRole.ROLE_ADMIN) {
			log.warn("Попытка смены роли администратора отклонена, targetUserId={}", targetUserId);
			throw new ForbiddenException("user.admin-change-role-error");
		}

		if (extractHighestRole(auth) == UserRole.ROLE_MANAGER && newRole == UserRole.ROLE_ADMIN) {
			log.warn("Менеджер не может назначить роль администратора, initiatorUserId={}", AuthUtil.id(auth));
			throw new ForbiddenException("common.forbidden");
		}

		targetUser.setRole(newRole);
		refreshJtiService.deactivateAllForUser(targetUser.getId());

		return repositoryService.save(targetUser).getId();
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

	private UserRole extractHighestRole(Authentication auth) {
		Set<String> authorities = auth.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toSet());

		if (authorities.contains(UserRole.ROLE_ADMIN.name())) {
			return UserRole.ROLE_ADMIN;
		}
		if (authorities.contains(UserRole.ROLE_MANAGER.name())) {
			return UserRole.ROLE_MANAGER;
		}
		return UserRole.ROLE_USER;
	}
}
