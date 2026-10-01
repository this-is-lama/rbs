package my.project.bookingservice.consistency.outbox.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import my.project.bookingservice.consistency.outbox.repository.OutboxBookingRepository;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OutboxMetrics {

	private final OutboxBookingRepository repository;
	private final MeterRegistry meterRegistry;

	@PostConstruct
	void registerGauges() {
		Gauge.builder("outbox.pending", repository, OutboxBookingRepository::count)
			.description("Сколько событий outbox ждут отправки в Kafka")
			.register(meterRegistry);

		Gauge.builder("outbox.oldest.age", this, OutboxMetrics::oldestAgeSeconds)
			.baseUnit("seconds")
			.description("Возраст самого старого неотправленного события outbox")
			.register(meterRegistry);
	}

	double oldestAgeSeconds() {
		return repository.findFirstByOrderByCreatedAtAsc()
			.map(event -> (double) Duration.between(event.getCreatedAt(), Instant.now()).getSeconds())
			.orElse(0.0);
	}
}
