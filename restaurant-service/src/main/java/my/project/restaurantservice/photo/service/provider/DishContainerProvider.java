package my.project.restaurantservice.photo.service.provider;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dish.repository.DishRepositoryService;
import my.project.restaurantservice.photo.entity.PhotoContainer;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishContainerProvider implements PhotoContainerProvider {

    private static final ContainerType CONTAINER_TYPE = ContainerType.DISHES;
    private static final String BUCKET_NAME = "dish-media";

    private final DishRepositoryService dishService;

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
