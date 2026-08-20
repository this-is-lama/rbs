package my.project.restaurantservice.client;

import my.project.restaurantservice.config.FeignConfig;
import my.project.restaurantservice.dto.client.UserDto;
import my.project.restaurantservice.dto.manager.ChangeRoleByIdRequest;
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

    @PostMapping("/api/v1/users/change-role-by-id")
    UUID changeRoleById(@RequestBody ChangeRoleByIdRequest req);

    @PostMapping("/api/v1/users")
    List<UserDto> getUsersByIds(@RequestBody Set<UUID> ids);
}