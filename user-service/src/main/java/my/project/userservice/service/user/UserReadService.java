package my.project.userservice.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.NotFoundException;
import my.project.common.security.AuthUtil;
import my.project.userservice.dto.UserDto;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.mapper.UserMapper;
import my.project.userservice.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserReadService {

	private final UserMapper mapper;

	private final UserPersistenceService persistenceService;


	public boolean existsByEmail(String email) {
		return persistenceService.existsByEmail(email);
	}

	public UserEntity findByEmail(String email) {
		return persistenceService.findByEmail(email);
	}

	public UserEntity findById(UUID id) {
		return persistenceService.findById(id);
	}

	public UserDto getMe(Authentication auth) {
		UUID userId = AuthUtil.id(auth);
		log.info("Получение профиля текущего пользователя, userId={}", userId);
		return mapper.toDto(findById(userId));
	}

	public UserDto getUserByEmail(String email) {
		log.info("Информация об пользователе по email={}", email);
		return mapper.toDto(findByEmail(email));
	}

	public UserDto getUserById(UUID id) {
		log.info("Информация об пользователе по id={}", id);
		return mapper.toDto(findById(id));
	}

	public List<UserDto> getUsersByIds(Set<UUID> ids) {
		if (ids == null || ids.isEmpty()) {
			log.info("Запрошены пользователи с пустым списком ids");
			return List.of();
		}

		log.info("Получение пользователей, count={}", ids.size());

		List<UserEntity> users = persistenceService.findAllById(ids);
		Map<UUID, UserDto> mapped = mapper.toDto(users).stream()
				.collect(Collectors.toMap(UserDto::id, Function.identity()));

		return ids.stream()
				.map(mapped::get)
				.filter(Objects::nonNull)
				.toList();
	}
}
