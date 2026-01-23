package my.project.restaurantservice.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class KeyGenerator {

	 private static final String KEY_TEMPLATE = "%s/%s.%s";

	public String generateKey(UUID entityId, String contentType) {
		UUID id = UUID.randomUUID();
		return KEY_TEMPLATE.formatted(entityId, id, getContentType(contentType));
	}

	private String getContentType(String type) {
		return switch (type) {
			case "image/jpeg" -> "jpeg";
			case "image/png" -> "png";
			case "image/webp" -> "webp";
			default -> throw new IllegalStateException("Unexpected value: " + type);
		};
	}
}
