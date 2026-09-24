package my.project.telegrambotservice.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record User(
		long id,
		@JsonProperty("is_bot") boolean isBot,
		@JsonProperty("first_name") String firstName,
		@JsonProperty("last_name") String lastName,
		String username
) {

	public String displayName() {
		String name = lastName == null || lastName.isBlank() ? firstName : firstName + " " + lastName;
		return name == null || name.isBlank() ? "Пользователь " + id : name;
	}
}
