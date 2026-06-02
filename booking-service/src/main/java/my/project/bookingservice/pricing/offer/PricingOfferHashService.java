package my.project.bookingservice.pricing.offer;

import my.project.bookingservice.pricing.dto.request.PricingPreorderItemRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PricingOfferHashService {
	public String hash(UUID userId, UUID restaurantId, UUID tableId, Instant visitStart, Instant visitEnd,
					   List<PricingPreorderItemRequest> items) {
		String normalizedItems = (items == null ? List.<PricingPreorderItemRequest>of() : items).stream()
				.sorted(Comparator.comparing(PricingPreorderItemRequest::dishId))
				.map(item -> item.dishId() + ":" + item.quantity())
				.collect(Collectors.joining("|"));
		String source = userId + ";" + restaurantId + ";" + tableId + ";" + visitStart + ";" + visitEnd + ";" + normalizedItems;
		return sha256(source);
	}

	private String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte b : hash) {
				hex.append(String.format("%02x", b));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 algorithm is not available", ex);
		}
	}
}

