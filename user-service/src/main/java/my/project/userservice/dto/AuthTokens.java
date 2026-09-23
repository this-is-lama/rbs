package my.project.userservice.dto;

public record AuthTokens(String accessToken, String refreshToken) {

	@Override
	public String toString() {
		return "AuthTokens[accessToken=***, refreshToken=***]";
	}
}
