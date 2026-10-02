package my.project.restaurantservice.contact.util;

import my.project.restaurantservice.contact.entity.ContactType;

/**
 * Приводит значение контакта к единому виду. Сейчас нормализуются только телефоны:
 * российские номера (8XXXXXXXXXX, 7XXXXXXXXXX, XXXXXXXXXX) превращаются в +7XXXXXXXXXX,
 * номера с международным префиксом "+" остаются в виде +<цифры>. Остальные значения не меняются.
 */
public final class ContactValueNormalizer {

	private ContactValueNormalizer() {
	}

	public static String normalize(ContactType type, String value) {
		if (value == null || type != ContactType.PHONE) {
			return value;
		}
		return normalizePhone(value.trim());
	}

	private static String normalizePhone(String raw) {
		String digits = raw.replaceAll("\\D", "");

		if (digits.length() == 11 && (digits.startsWith("7") || digits.startsWith("8"))) {
			return "+7" + digits.substring(1);
		}
		if (digits.length() == 10) {
			return "+7" + digits;
		}
		if (raw.startsWith("+") && digits.length() >= 8 && digits.length() <= 15) {
			return "+" + digits;
		}
		return raw;
	}
}
