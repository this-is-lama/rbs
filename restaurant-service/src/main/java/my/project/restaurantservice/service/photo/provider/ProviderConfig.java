package my.project.restaurantservice.service.photo.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class ProviderConfig {

    @Bean
    public Map<ContainerType, PhotoContainerProvider> photoProviders(List<PhotoContainerProvider> list) {
        return list.stream()
                .collect(
                        Collectors.toUnmodifiableMap(PhotoContainerProvider::type, p -> p)
                );
    }
}
