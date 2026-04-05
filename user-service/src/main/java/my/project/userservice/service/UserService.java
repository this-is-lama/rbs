package my.project.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
		log.debug("Загрузка пользователя для аутентификации, email={}", email);

		UserEntity user = findByEmail(email);
		if (!user.isEnabled()) {
			log.warn("Попытка входа отключённого пользователя, userId={}, email={}", user.getId(), email);
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
		log.info("Получение пользователя по id={}", id);
		var user = findById(id);
		return mapper.toDto(user);
	}

	@Transactional(readOnly = true)
	public UserEntity findById(UUID id) {
		return repository.findById(id)
				.orElseThrow(() -> {
					log.warn("Пользователь не найден по id={}", id);
					return new NotFoundException("user.not-found-by-id", id);
				});
	}

	@Transactional(readOnly = true)
	public UserEntity findByEmail(String email) {
		return repository.findByEmail(email)
				.orElseThrow(() -> {
					log.warn("Пользователь не найден по email={}", email);
					return new NotFoundException("user.not-found-by-email", email);
				});
	}

	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		boolean exists = repository.existsByEmail(email);
		log.debug("Проверка существования пользователя по email={}, результат={}", email, exists);
		return exists;
	}

	@Transactional
	public UserEntity save(RegistrationRequest req) {
		log.info("Сохранение нового пользователя, email={}", req.email());
		UserEntity user = mapper.toEntity(req, passwordEncoder);
		UserEntity savedUser = repository.save(user);
		log.info("Пользователь сохранён, userId={}, email={}", savedUser.getId(), savedUser.getEmail());
		return savedUser;
	}

	@Transactional
	public UUID changeRoleByEmail(ChangeRoleRequest req, Authentication auth) {
		UserRole newRole = req.role();
		UserEntity user = findByEmail(req.email());

		log.info("Попытка смены роли пользователя, userId={}, email={}, новая роль={}",
				user.getId(), user.getEmail(), newRole);

		if (AuthUtil.isManager(auth) && newRole == UserRole.ROLE_ADMIN) {
			log.warn("Менеджеру запрещено назначать роль администратора, email={}", req.email());
			throw new ForbiddenException("user.admin-change-role-error");
		}

		if (user.getRole() == UserRole.ROLE_ADMIN) {
			log.warn("Попытка изменения роли администратора отклонена, email={}", req.email());
			throw new BadRequestException("user.admin-change-role-error");
		}

		user.setRole(newRole);
		refreshJtiService.deactivateAllForUser(user.getId());

		UUID savedId = repository.save(user).getId();
		log.info("Роль пользователя успешно изменена, userId={}, новая роль={}", savedId, newRole);

		return savedId;
	}

	@Transactional
	public UserDto update(UUID userId, UpdateUserRequest req) {
		log.info("Обновление профиля пользователя, userId={}", userId);

		UserEntity user = findById(userId);
		boolean emailChanged = !user.getEmail().equals(req.email());

		if (emailChanged && existsByEmail(req.email())) {
			log.warn("Обновление профиля отклонено: email={} уже занят", req.email());
			throw new ConflictException("user.email-already-use");
		}

		mapper.updateEntity(req, user);
		UserEntity savedUser = repository.save(user);

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

		UserEntity user = findById(userId);

		if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
			log.warn("Текущий пароль указан неверно, userId={}", userId);
			throw new BadRequestException("user.invalid-current-password");
		}

		if (passwordEncoder.matches(req.newPassword(), user.getPasswordHash())) {
			log.warn("Новый пароль совпадает со старым, userId={}", userId);
			throw new BadRequestException("user.new-password-must-differ");
		}

		user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
		repository.save(user);

		refreshJtiService.deactivateAllForUser(user.getId());

		log.info("Пароль пользователя успешно изменён, userId={}", userId);
	}
}