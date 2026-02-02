package my.project.restaurantservice.service.photo.provider;

import my.project.restaurantservice.entity.PhotoContainer;

import java.util.UUID;

public record ProviderContext (
		ContainerType type,
		String bucket,
		PhotoContainer container,
		UUID accessContainerId
) {}
