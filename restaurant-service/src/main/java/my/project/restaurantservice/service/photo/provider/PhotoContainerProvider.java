package my.project.restaurantservice.service.photo.provider;

import my.project.restaurantservice.entity.PhotoContainer;

import java.util.UUID;

public interface PhotoContainerProvider {

    OwnerType type();

    String bucket();

    PhotoContainer getRef(UUID ownerId);
}
