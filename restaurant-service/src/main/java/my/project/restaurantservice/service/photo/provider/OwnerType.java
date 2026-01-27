package my.project.restaurantservice.service.photo.provider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OwnerType {

    RESTAURANT,
    DISH;

    public static OwnerType fromPath(String container) {
        if (container == null) throw new IllegalArgumentException("container is null");
        return switch (container.toLowerCase()) {
            case "restaurants" -> RESTAURANT;
            case "dishes" -> DISH;
            default -> throw new IllegalArgumentException("Unsupported container: " + container);
        };
    }

}

