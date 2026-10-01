package my.project.restaurantservice.kafka;

import my.project.restaurantservice.manager.event.ManagerRestaurantsChangedEvent;

public interface KafkaProducer {

	void sendManagerRestaurantsChanged(ManagerRestaurantsChangedEvent event);
}
