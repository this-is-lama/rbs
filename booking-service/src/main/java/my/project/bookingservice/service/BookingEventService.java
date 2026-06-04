package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.dto.response.BookingUserResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.kafka.KafkaProducer;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.common.security.AuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingEventService {
	private final KafkaProducer kafkaProducer;
	private final BookingMapper mapper;
	private final UserBriefCacheService userBriefCacheService;

	public void sendCreated(BookingEntity booking, Authentication auth) {
		kafkaProducer.sendBookingCreated(mapper.toEvent(booking, AuthUtil.email(auth), AuthUtil.username(auth)));
	}

	public void sendCancelled(BookingEntity booking, String reason) {
		BookingUserResponse user = userBriefCacheService.getUserBrief(booking.getUserId());
		String username = user.name() + " " + user.surname();
		kafkaProducer.sendBookingCancelled(mapper.toCancelledEvent(booking, user.email(), username, reason));
	}
}
