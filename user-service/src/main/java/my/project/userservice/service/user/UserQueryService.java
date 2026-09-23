package my.project.userservice.service.user;

import lombok.RequiredArgsConstructor;
import my.project.common.logging.Loggable;
import my.project.userservice.dto.UserDto;
import my.project.userservice.mapper.UserMapper;
import my.project.userservice.repository.UserRepositoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Loggable
@Service
@RequiredArgsConstructor
public class UserQueryService {

	private final UserRepositoryService repositoryService;
	private final UserMapper mapper;

	public UserDto getById(UUID id) {
		return mapper.toDto(repositoryService.getById(id));
	}

	public UserDto getByEmail(String email) {
		return mapper.toDto(repositoryService.getByEmail(email));
	}

	public List<UserDto> findAllByIds(Set<UUID> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}

		Map<UUID, UserDto> mapped = mapper.toDto(repositoryService.findAllById(ids)).stream()
				.collect(Collectors.toMap(UserDto::id, Function.identity()));

		return ids.stream()
				.map(mapped::get)
				.filter(Objects::nonNull)
				.toList();
	}
}
