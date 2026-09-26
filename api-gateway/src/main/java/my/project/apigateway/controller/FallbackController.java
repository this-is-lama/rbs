package my.project.apigateway.controller;

import my.project.apigateway.dto.ApiError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    private static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";

    @RequestMapping("/fallback/booking")
    public Mono<ResponseEntity<ApiError>> booking() {
        return Mono.just(ResponseEntity.status(503)
                .body(new ApiError(503,
                        SERVICE_UNAVAILABLE ,
                        "Бронирование временно недоступно",
                        "/api/v1/bookings/**")
                )
        );
    }

    @RequestMapping("/fallback/restaurants")
    public Mono<ResponseEntity<ApiError>> restaurant() {
        return Mono.just(ResponseEntity.status(503)
                .body(new ApiError(503,
                        SERVICE_UNAVAILABLE ,
                        "Сервис ресторанов временно недоступен",
                        "/api/v1/restaurants/**")
                )
        );
    }

    @RequestMapping("/fallback/users")
    public Mono<ResponseEntity<ApiError>> user() {
        return Mono.just(ResponseEntity.status(503)
                .body(new ApiError(503,
                        SERVICE_UNAVAILABLE ,
                        "Сервис пользователей временно недоступен",
                        "/api/v1/users/**")
                )
        );
    }
}