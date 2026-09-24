package my.project.telegrambotservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Ограничения сценария обращения в поддержку.
 */
@ConfigurationProperties(prefix = "app.support")
public record SupportProperties(
		int maxTicketsPerDay,
		int maxAttachments,
		int minTextLength,
		int maxTextLength,
		Duration draftTtl,
		String siteUrl
) {
}
