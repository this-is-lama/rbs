package my.project.bookingservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
public class CacheConfig {
	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
		RedisSerializationContext.SerializationPair<String> keySerializer =
				RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer());
		RedisSerializationContext.SerializationPair<Object> valueSerializer =
				RedisSerializationContext.SerializationPair.fromSerializer(
						new GenericJackson2JsonRedisSerializer(cacheObjectMapper(objectMapper))
				);

		RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
				.serializeKeysWith(keySerializer)
				.serializeValuesWith(valueSerializer)
				.disableCachingNullValues();

		Map<String, RedisCacheConfiguration> cacheConfigs = Map.ofEntries(
				Map.entry("pricingOffersById", defaultConfig.entryTtl(Duration.ofMinutes(10))),
				Map.entry("pricingOffersByHash", defaultConfig.entryTtl(Duration.ofMinutes(10))),
				Map.entry("bookingAvailability", defaultConfig.entryTtl(Duration.ofMinutes(2))),
				Map.entry("managerAccess", defaultConfig.entryTtl(Duration.ofMinutes(5))),
				Map.entry("userBriefs", defaultConfig.entryTtl(Duration.ofMinutes(15))),
				Map.entry("restaurantBookings", defaultConfig.entryTtl(Duration.ofSeconds(30))),
				Map.entry("pricingHistoryAggregates", defaultConfig.entryTtl(Duration.ofMinutes(15))),
				Map.entry("pricingWeights", defaultConfig.entryTtl(Duration.ofMinutes(30))),
				Map.entry("calendarCoefficients", defaultConfig.entryTtl(Duration.ofMinutes(30))),
				Map.entry("pricingCalendarDays", defaultConfig.entryTtl(Duration.ofDays(14)))
		);

		return RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(defaultConfig)
				.withInitialCacheConfigurations(cacheConfigs)
				.transactionAware()
				.build();
	}

	private ObjectMapper cacheObjectMapper(ObjectMapper objectMapper) {
		ObjectMapper mapper = objectMapper.copy();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.activateDefaultTyping(
				BasicPolymorphicTypeValidator.builder()
						.allowIfSubType("my.project.bookingservice")
						.allowIfSubType("java.lang")
						.allowIfSubType("java.util")
						.allowIfSubType("java.math")
						.allowIfSubType("java.time")
						.build(),
				ObjectMapper.DefaultTyping.EVERYTHING
		);
		return mapper;
	}
}
