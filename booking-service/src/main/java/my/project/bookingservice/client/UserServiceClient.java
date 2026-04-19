package my.project.bookingservice.client;

import my.project.bookingservice.config.FeignConfig;
import my.project.bookingservice.dto.client.UserBriefDto;
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

    @PostMapping("/api/v1/users/briefs")
    List<UserBriefDto> getBriefs(@RequestBody Set<UUID> ids);
}