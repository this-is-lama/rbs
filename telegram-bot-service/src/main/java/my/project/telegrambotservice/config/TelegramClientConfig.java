package my.project.telegrambotservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class TelegramClientConfig {

	@Bean
	public RestClient telegramRestClient(TelegramProperties properties) {
		HttpClient httpClient = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1)
				.connectTimeout(Duration.ofSeconds(10))
				.build();

		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		// getUpdates висит до pollTimeout, поэтому таймаут чтения чуть больше
		requestFactory.setReadTimeout(properties.pollTimeout().plusSeconds(15));

		return RestClient.builder()
				.baseUrl(properties.apiUrl() + "/bot" + properties.token())
				.requestFactory(requestFactory)
				.build();
	}
}
