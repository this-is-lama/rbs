package my.project.userservice.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.BadRequestException;
import my.project.common.exception.ConflictException;
import my.project.common.exception.ForbiddenException;
import my.project.common.security.AuthUtil;
import my.project.common.security.UserRole;
import my.project.userservice.dto.*;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.mapper.UserMapper;
import my.project.userservice.service.jwt.RefreshJtiService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

	private final UserMapper mapper;

	private final UserReadService readService;
	private final UserPersistenceService persistenceService;

	private final RefreshJtiService refreshJtiService;
	private final PasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(String email) {
		log.debug("Загрузка пользователя для аутентификации, email={}", email);

		UserEntity user = readService.findByEmail(email);
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



	@Transactional
	public UserEntity save(RegistrationRequest req) {
		log.info("Создание пользователя, email={}", req.email());
		UserEntity user = mapper.toEntity(req, passwordEncoder);
		return persistenceService.save(user);
	}



	@Transactional
	public UUID changeRoleById(ChangeRoleByIdRequest req, Authentication auth) {
		UUID currentUserId = AuthUtil.id(auth);
		UserRole currentRole = extractHighestRole(auth);

		UUID targetUserId = req.userId();
		UserRole newRole = req.role();

		log.info("Смена роли пользователя, initiatorUserId={}, targetUserId={}, newRole={}",
				currentUserId, targetUserId, newRole);

		UserEntity targetUser = readService.findById(targetUserId);

		if (targetUser.getRole() == UserRole.ROLE_ADMIN) {
			log.warn("Попытка смены роли администратора отклонена, targetUserId={}", targetUserId);
			throw new ForbiddenException("user.admin-change-role-error");
		}

		if (currentRole == UserRole.ROLE_MANAGER && newRole == UserRole.ROLE_ADMIN) {
			log.warn("Менеджер не может назначить роль администратора, initiatorUserId={}", currentUserId);
			throw new ForbiddenException("common.forbidden");
		}

		targetUser.setRole(newRole);
		refreshJtiService.deactivateAllForUser(targetUser.getId());

		UUID savedId =  persistenceService.save(targetUser).getId();
		log.info("Роль пользователя успешно изменена, userId={}, новая роль={}", savedId, newRole);

		return savedId;
	}

	@Transactional
	public UserDto update(UUID userId, UpdateUserRequest req) {
		log.info("Обновление профиля пользователя, userId={}", userId);

		UserEntity user = readService.findById(userId);
		boolean emailChanged = !user.getEmail().equals(req.email());

		if (emailChanged && readService.existsByEmail(req.email())) {
			log.warn("Обновление профиля отклонено: email={} уже занят", req.email());
			throw new ConflictException("user.email-already-use");
		}

		mapper.updateEntity(req, user);
		UserEntity savedUser =  persistenceService.save(user);

		if (emailChanged) {
			log.info("Email пользователя был изменён, деактивируются все refresh токены, userId={}", savedUser.getId());
			refreshJtiService.deactivateAllForUser(savedUser.getId());
		}

		log.info("Профиль пользователя успешно обновлён, userId={}", savedUser.getId());
		return mapper.toDto(savedUser);
	}

	@Transactional
	public void changePassword(UUID userId, ChangePasswordRequest req) {
		log.info("Смена пароля пользователя, userId={}", userId);

		UserEntity user = readService.findById(userId);

		if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
			log.warn("Текущий пароль указан неверно, userId={}", userId);
			throw new BadRequestException("user.invalid-current-password");
		}

		if (passwordEncoder.matches(req.newPassword(), user.getPasswordHash())) {
			log.warn("Новый пароль совпадает со старым, userId={}", userId);
			throw new BadRequestException("user.new-password-must-differ");
		}

		user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
		persistenceService.save(user);

		refreshJtiService.deactivateAllForUser(user.getId());

		log.info("Пароль пользователя успешно изменён, userId={}", userId);
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