package my.project.restaurantservice.service.photo.provider;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.entity.PhotoContainer;
import my.project.restaurantservice.service.restaurant.RestaurantService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantContainerProvider implements PhotoContainerProvider {

    private final RestaurantService restaurantService;

    private static final ContainerType CONTAINER_TYPE = ContainerType.RESTAURANT;
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
