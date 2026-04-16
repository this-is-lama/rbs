package my.project.restaurantservice.service.photo.provider;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.entity.PhotoContainer;
import my.project.restaurantservice.service.dish.DishService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishContainerProvider implements PhotoContainerProvider {

    private static final ContainerType CONTAINER_TYPE = ContainerType.DISHES;
    private static final String BUCKET_NAME = "dish-media";

    private final DishService dishService;

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
        return dishService.getRef(containerId);
    }

    @Override
    public ProviderContext context(UUID containerId) {
        var dish = dishService.getRef(containerId);
        return new ProviderContext(CONTAINER_TYPE, bucket(), dish, dish.getRestaurant().getId());
    }
}
