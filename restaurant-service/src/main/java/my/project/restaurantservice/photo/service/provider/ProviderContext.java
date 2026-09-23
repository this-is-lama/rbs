package my.project.restaurantservice.photo.service.provider;

import my.project.restaurantservice.photo.entity.PhotoContainer;

import java.util.UUID;

public record ProviderContext (
		ContainerType type,
		String bucket,
		PhotoContainer container,
		UUID accessContainerId
) {}
