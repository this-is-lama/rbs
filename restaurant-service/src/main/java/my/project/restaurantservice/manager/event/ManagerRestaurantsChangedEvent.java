package my.project.restaurantservice.manager.event;

import java.util.UUID;

public record ManagerRestaurantsChangedEvent(

		UUID managerId,

		boolean hasRestaurants

) {}
