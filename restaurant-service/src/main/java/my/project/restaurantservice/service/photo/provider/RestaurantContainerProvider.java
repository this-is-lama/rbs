package my.project.restaurantservice.service.photo.provider;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.entity.PhotoContainer;
import my.project.restaurantservice.service.RestaurantService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantContainerProvider implements PhotoContainerProvider {

    private final RestaurantService restaurantService;

    @Override
    public OwnerType type() {
        return OwnerType.RESTAURANT;
    }

    @Override
    public String bucket() {
        return "restaurant-media";
    }

    @Override
    public PhotoContainer getRef(UUID ownerId) {
        return restaurantService.getRef(ownerId);
    }
}
