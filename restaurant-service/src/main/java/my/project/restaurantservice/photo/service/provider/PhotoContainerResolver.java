package my.project.restaurantservice.photo.service.provider;

import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.CommonErrorCode;
import my.project.common.exception.ValidationException;
import my.project.restaurantservice.photo.entity.PhotoContainer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PhotoContainerResolver {

    private final Map<ContainerType, PhotoContainerProvider> providers;

    public PhotoContainerResolver(List<PhotoContainerProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toUnmodifiableMap(PhotoContainerProvider::type, Function.identity()));
    }

    public ProviderContext context(ContainerType type, UUID containerId) {
        return provider(type).context(containerId);
    }

    public PhotoContainer getRef(ContainerType type, UUID containerId) {
        return provider(type).getRef(containerId);
    }

    private PhotoContainerProvider provider(ContainerType type) {
        PhotoContainerProvider provider = providers.get(type);
        if (provider == null) {
            log.warn("Неподдерживаемый тип контейнера для фотографий: {}", type);
            throw new ValidationException(CommonErrorCode.BAD_REQUEST, "restaurant.photo.unsupported-owner-type", type);
        }
        return provider;
    }
}
