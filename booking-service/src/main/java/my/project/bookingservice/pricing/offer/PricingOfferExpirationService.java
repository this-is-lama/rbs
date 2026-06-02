package my.project.bookingservice.pricing.offer;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PricingOfferExpirationService {
	public boolean isActual(Instant expiresAt) {
		return expiresAt != null && Instant.now().isBefore(expiresAt);
	}
}

