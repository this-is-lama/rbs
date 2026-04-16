package my.project.restaurantservice.client;

import my.project.restaurantservice.dto.manager.ChangeRoleByIdRequest;
import my.project.restaurantservice.dto.manager.ChangeRoleRequest;
import my.project.restaurantservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @PatchMapping("/api/v1/users/change-role")
    UUID changeRole(@RequestBody ChangeRoleRequest req);

    @PatchMapping("/api/v1/users/change-role-by-id")
    UUID changeRoleById(@RequestBody ChangeRoleByIdRequest req);

    @PostMapping("/api/v1/users/summaries")
    List<UserLookupDto> getSummaries(@RequestBody Set<UUID> ids);

}
    