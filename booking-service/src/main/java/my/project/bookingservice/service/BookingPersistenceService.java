package my.project.bookingservice.service;

import my.project.bookingservice.entity.BookingEntity;

public interface BookingPersistenceService {

	BookingEntity save(BookingEntity booking);

	BookingEntity cancel(BookingEntity booking, String reason);
}
