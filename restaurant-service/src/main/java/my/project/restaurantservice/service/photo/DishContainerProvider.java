package my.project.restaurantservice.service.photo;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.entity.PhotoContainer;
import my.project.restaurantservice.service.DishService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishContainerProvider implements PhotoContainerProvider {

    private final DishService dishService;

    @Override
    public OwnerType type() {
        return OwnerType.DISH;
    }

    @Override
    public String bucket() {
        return "dish-media";
    }

    @Override
    public PhotoContainer getRef(UUID ownerId) {
        return dishService.getRef(ownerId);
    }
}
