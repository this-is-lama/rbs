package my.project.restaurantservice.internal.client;

import my.project.restaurantservice.config.FeignConfig;
import my.project.restaurantservice.internal.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
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

    @PostMapping("/api/v1/users")
    List<UserDto> getUsersByIds(@RequestBody Set<UUID> ids);
}