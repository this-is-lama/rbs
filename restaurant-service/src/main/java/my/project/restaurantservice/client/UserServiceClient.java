package my.project.restaurantservice.client;

import my.project.common.dto.ChangeRoleRequest;
import my.project.restaurantservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(
        name = "user-service",
        configuration = FeignConfig.class
)
public interface UserServiceClient {

    @PatchMapping("/api/v1/users/change-role")
    UUID changeRole(@RequestBody ChangeRoleRequest req);

}
    