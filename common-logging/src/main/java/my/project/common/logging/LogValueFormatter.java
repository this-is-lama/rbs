package my.project.common.logging;

import org.springframework.util.ClassUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

final class LogValueFormatter {

	private static final int MAX_LENGTH = 300;

	private static final String AUTHENTICATION_CLASS_NAME = "org.springframework.security.core.Authentication";
	private static final Class<?> AUTHENTICATION_CLASS = ClassUtils.isPresent(AUTHENTICATION_CLASS_NAME, null)
			? ClassUtils.resolveClassName(AUTHENTICATION_CLASS_NAME, null)
			: null;

	private LogValueFormatter() {
	}

	static String formatArgs(String[] names, Object[] args) {
		StringJoiner joiner = new StringJoiner(", ");
		for (int i = 0; i < args.length; i++) {
			String name = names != null && i < names.length ? names[i] : "arg" + i;
			joiner.add(name + "=" + format(args[i]));
		}
		return joiner.toString();
	}

	static String format(Object value) {
		if (value == null) {
			return "null";
		}
		if (AUTHENTICATION_CLASS != null && AUTHENTICATION_CLASS.isInstance(value)) {
			return AuthenticationFormatter.format(value);
		}
		if (value instanceof Collection<?> collection) {
			return collectionType(collection) + "[size=" + collection.size() + "]";
		}
		if (value instanceof Map<?, ?> map) {
			return "Map[size=" + map.size() + "]";
		}
		if (value.getClass().isArray()) {
			return value.getClass().getComponentType().getSimpleName() + "[" + Array.getLength(value) + "]";
		}
		return truncate(String.valueOf(value));
	}

	private static String collectionType(Collection<?> collection) {
		if (collection instanceof List<?>) {
			return "List";
		}
		if (collection instanceof Set<?>) {
			return "Set";
		}
		return "Collection";
	}

	private static String truncate(String value) {
		if (value.length() <= MAX_LENGTH) {
			return value;
		}
		return value.substring(0, MAX_LENGTH) + "...(" + value.length() + " символов)";
	}
}
