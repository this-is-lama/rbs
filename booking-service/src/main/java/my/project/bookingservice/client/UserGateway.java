package my.project.bookingservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import my.project.bookingservice.dto.client.UserDto;
import my.project.common.exception.ApiException;
import my.project.common.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserGateway {

	private final UserServiceClient client;

	@Retry(name = "userService", fallbackMethod = "userFallback")
	@CircuitBreaker(name = "userService")
	public UserDto getUserById(UUID id) {
		return client.getUserById(id);
	}

	private UserDto userFallback(UUID id, Throwable ex) {
		if (ex instanceof ApiException apiEx) throw apiEx;
		throw new ServiceUnavailableException("Сервис пользователей временно не доступен");
	}

	@Retry(name = "userService", fallbackMethod = "usersFallback")
	@CircuitBreaker(name = "userService")
	public List<UserDto> getUsersByIds(Set<UUID> ids) {
		return client.getUsersByIds(ids);
	}

	private List<UserDto> usersFallback(Set<UUID> ids, Throwable ex) {
		if (ex instanceof ApiException apiEx) throw apiEx;
		throw new ServiceUnavailableException("Сервис пользователей временно не доступен");
	}
}
