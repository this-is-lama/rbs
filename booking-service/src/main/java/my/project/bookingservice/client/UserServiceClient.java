package my.project.bookingservice.client;

import my.project.bookingservice.config.FeignConfig;
import my.project.bookingservice.dto.client.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@FeignClient(
        name = "user-service",
        configuration = FeignConfig.class
)
public interface UserServiceClient {

    @PostMapping("/api/v1/users/{id}")
    UserDto getUserById(@PathVariable UUID id);

    @PostMapping()
    List<UserDto> getUsersByIds(@RequestBody Set<UUID> ids);
}