package my.project.restaurantservice.photo.service.provider;

import my.project.restaurantservice.photo.entity.PhotoContainer;

import java.util.UUID;

public interface PhotoContainerProvider {

    ContainerType type();

    String bucket();

    PhotoContainer getRef(UUID containerId);

    ProviderContext context(UUID containerId);

}
