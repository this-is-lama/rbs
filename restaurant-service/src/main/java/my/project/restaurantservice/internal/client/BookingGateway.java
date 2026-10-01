package my.project.restaurantservice.internal.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import my.project.common.exception.ApiException;
import my.project.common.exception.ServiceUnavailableException;
import my.project.restaurantservice.internal.dto.BookingDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingGateway {

	private final BookingServiceClient client;

	@Retry(name = "bookingService", fallbackMethod = "bookingsFallback")
	@CircuitBreaker(name = "bookingService")
	public List<BookingDto> getRestaurantBookings(UUID restId) {
		return client.getRestaurantBookings(restId);
	}

	private List<BookingDto> bookingsFallback(UUID restId, Throwable ex) {
		if (ex instanceof ApiException apiEx) throw apiEx;
		throw new ServiceUnavailableException("Сервис бронирований временно не доступен");
	}
}
