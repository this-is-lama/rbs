package my.project.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.BadRequestException;
import my.project.common.exception.ConflictException;
import my.project.common.exception.ForbiddenException;
import my.project.common.exception.NotFoundException;
import my.project.common.security.AuthUtil;
import my.project.common.security.UserRole;
import my.project.userservice.dto.ChangePasswordRequest;
import my.project.userservice.dto.ChangeRoleByIdRequest;
import my.project.userservice.dto.RegistrationRequest;
import my.project.userservice.dto.UpdateUserRequest;
import my.project.userservice.dto.UserBriefDto;
import my.project.userservice.dto.UserDto;
import my.project.userservice.dto.UserLookupDto;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.mapper.UserMapper;
import my.project.userservice.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return repository.existsByEmail(email);
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
	public UserEntity findById(UUID id) {
		return repository.findById(id)
				.orElseThrow(() -> {
					log.warn("Пользователь не найден по id={}", id);
					return new NotFoundException("user.not-found-by-id", id);
				});
	}

	@Transactional
	public UserEntity save(RegistrationRequest req) {
		log.info("Создание пользователя, email={}", req.email());
		UserEntity user = mapper.toEntity(req, passwordEncoder);
		return repository.save(user);
	}

	@Transactional(readOnly = true)
	public UserDto getMe(Authentication auth) {
		UUID userId = AuthUtil.id(auth);
		log.info("Получение профиля текущего пользователя, userId={}", userId);
		return mapper.toDto(findById(userId));
	}

	@Transactional(readOnly = true)
	public UserLookupDto lookupByEmail(String email) {
		log.info("Lookup пользователя по email={}", email);
		return mapper.toLookupDto(findByEmail(email));
	}

	@Transactional(readOnly = true)
	public List<UserLookupDto> getSummaries(Set<UUID> ids) {
		if (ids == null || ids.isEmpty()) {
			log.info("Запрошены summaries пользователей с пустым списком ids");
			return List.of();
		}

		log.info("Получение summaries пользователей, count={}", ids.size());

		List<UserEntity> users = repository.findAllById(ids);
		Map<UUID, UserLookupDto> mapped = mapper.toLookupDto(users).stream()
				.collect(Collectors.toMap(UserLookupDto::id, Function.identity()));

		return ids.stream()
				.map(mapped::get)
				.filter(Objects::nonNull)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<UserBriefDto> getBriefs(Set<UUID> ids) {
		if (ids == null || ids.isEmpty()) {
			log.info("Запрошены brief-пользователи с пустым списком ids");
			return List.of();
		}

		log.info("Получение краткой информации о пользователях, count={}", ids.size());

		List<UserEntity> users = repository.findAllById(ids);
		Map<UUID, UserBriefDto> mapped = mapper.toBriefDto(users).stream()
				.collect(Collectors.toMap(UserBriefDto::id, Function.identity()));

		return ids.stream()
				.map(mapped::get)
				.filter(Objects::nonNull)
				.toList();
	}

	@Transactional
	public UUID changeRoleById(ChangeRoleByIdRequest req, Authentication auth) {
		UUID currentUserId = AuthUtil.id(auth);
		UserRole currentRole = extractHighestRole(auth);

		UUID targetUserId = req.userId();
		UserRole newRole = req.role();

		log.info("Смена роли пользователя, initiatorUserId={}, targetUserId={}, newRole={}",
				currentUserId, targetUserId, newRole);

		UserEntity targetUser = findById(targetUserId);

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

		UUID savedId = repository.save(targetUser).getId();
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