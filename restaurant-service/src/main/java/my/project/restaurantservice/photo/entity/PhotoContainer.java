package my.project.restaurantservice.photo.entity;

import java.util.UUID;

public interface PhotoContainer {

	UUID getId();

	void addPhoto(PhotoEntity photo);

	void removePhoto(PhotoEntity photo);

}
