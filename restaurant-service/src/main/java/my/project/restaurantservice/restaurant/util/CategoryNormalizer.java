package my.project.restaurantservice.restaurant.util;

import java.util.Locale;

/**
 * Приводит категорию к единому виду: без лишних пробелов, всё строчными, первая буква заглавная.
 * Например, "  итальянская   КУХНЯ " -> "Итальянская кухня".
 */
public final class CategoryNormalizer {

	private CategoryNormalizer() {
	}

	public static String normalize(String category) {
		if (category == null) {
			return null;
		}
		String value = category.trim().replaceAll("\\s+", " ");
		if (value.isEmpty()) {
			return value;
		}
		value = value.toLowerCase(Locale.ROOT);
		int firstEnd = value.offsetByCodePoints(0, 1);
		return value.substring(0, firstEnd).toUpperCase(Locale.ROOT) + value.substring(firstEnd);
	}
}
