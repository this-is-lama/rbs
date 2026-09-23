package my.project.restaurantservice.photo.util;

import my.project.common.exception.CommonErrorCode;
import my.project.common.exception.ValidationException;

import java.util.Map;
import java.util.UUID;

public class KeyGenerator {

	private static final String KEY_TEMPLATE = "%s/%s.%s";

	private static final Map<String, String> EXT_BY_CONTENT_TYPE = Map.of(
			"image/jpeg", "jpeg",
			"image/png", "png",
			"image/webp", "webp"
	);

	public static String generateKey(UUID entityId, String contentType) {
		String ext = EXT_BY_CONTENT_TYPE.get(contentType);
		if (ext == null) {
			throw new ValidationException(CommonErrorCode.UNSUPPORTED_MEDIA_TYPE, "restaurant.storage.unsupported-content-type", contentType);
		}
		var id = UUID.randomUUID();
		return KEY_TEMPLATE.formatted(entityId, id, ext);
	}

}
