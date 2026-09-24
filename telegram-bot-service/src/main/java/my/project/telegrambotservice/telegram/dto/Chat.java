package my.project.telegrambotservice.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Chat(
		long id,
		String type
) {

	public boolean isPrivate() {
		return "private".equals(type);
	}
}
