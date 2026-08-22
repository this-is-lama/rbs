package my.project.userservice.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.NotFoundException;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPersistenceService {

	private final UserRepository repository;

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

	@Transactional(readOnly = true)
	public List<UserEntity> findAllById(Set<UUID> ids) {
		return repository.findAllById(ids);
	}

	@Transactional
	public UserEntity save(UserEntity user) {
		return repository.save(user);
	}
}
