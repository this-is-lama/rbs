package my.project.restaurantservice.service.photo.provider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ContainerType {

    RESTAURANT,
    DISH;

    public static ContainerType fromPath(String container) {
        if (container == null) throw new IllegalArgumentException("container is null");
        try {
            return valueOf(container.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported container: " + container);
        }
    }

}

