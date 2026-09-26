package my.project.bookingservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import my.project.bookingservice.dto.client.BookingSnapshotRequest;
import my.project.bookingservice.dto.client.BookingSnapshotResponse;
import my.project.common.exception.ApiException;
import my.project.common.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RestaurantGateway {

    private final RestaurantServiceClient client;

    @Retry(name = "restaurantService", fallbackMethod = "snapshotFallback")
    @CircuitBreaker(name = "restaurantService")
    public BookingSnapshotResponse bookingSnapshot(UUID restId, BookingSnapshotRequest req) {
        return client.bookingSnapshot(restId, req);
    }

    private BookingSnapshotResponse snapshotFallback(UUID restId, BookingSnapshotRequest req, Throwable ex) {
        if (ex instanceof ApiException apiEx) throw apiEx;   // 404 и т.п. пробрасываем как есть
        throw new ServiceUnavailableException("Сервис ресторанов временно недоступен"); // → 503
    }

    @Retry(name = "restaurantService", fallbackMethod = "managerFallback")
    @CircuitBreaker(name = "restaurantService")
    public boolean hasManagerAccess(UUID restId) {
        return client.hasManagerAccess(restId);
    }

    private boolean managerFallback(UUID restId, Throwable ex) {
        if (ex instanceof ApiException apiEx) throw apiEx;
        throw new ServiceUnavailableException("Сервис ресторанов временно недоступен");
    }
}