package my.project.restaurantservice.photo.service.provider;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.photo.entity.PhotoContainer;
import my.project.restaurantservice.restaurant.repository.RestaurantRepositoryService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantContainerProvider implements PhotoContainerProvider {

    private final RestaurantRepositoryService restaurantService;

    private static final ContainerType CONTAINER_TYPE = ContainerType.RESTAURANTS;
    private static final String BUCKET_NAME = "restaurant-media";

    @Override
    public ContainerType type() {
        return CONTAINER_TYPE;
    }

    @Override
    public String bucket() {
        return BUCKET_NAME;
    }

    @Override
    public PhotoContainer getRef(UUID containerId) {
        return restaurantService.getRef(containerId);
    }

    @Override
    public ProviderContext context(UUID containerId) {
        var restaurant = getRef(containerId);
        return new ProviderContext(CONTAINER_TYPE, BUCKET_NAME, restaurant, restaurant.getId());
    }
}
