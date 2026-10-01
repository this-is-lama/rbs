package my.project.userservice.events;

import java.util.UUID;

public record ManagerRestaurantsChangedEvent(

		UUID managerId,

		boolean hasRestaurants

) {}
