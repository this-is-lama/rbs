package my.project.telegrambotservice.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Обёртка любого ответа Bot API: {"ok": true, "result": ...} или {"ok": false, "error_code": 403, "description": "..."}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiResponse<T>(
		boolean ok,
		T result,
		@JsonProperty("error_code") Integer errorCode,
		String description
) {
}
