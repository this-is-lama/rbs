package my.project.restaurantservice.config;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        GenericJacksonJsonRedisSerializer serializer = GenericJacksonJsonRedisSerializer.builder()
                .enableUnsafeDefaultTyping()
                .build();

        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(RedisCacheConfiguration base) {
        return builder -> builder
            .cacheDefaults(base.entryTtl(Duration.ofMinutes(10)))

            .withCacheConfiguration("publicRestaurantById", base.entryTtl(Duration.ofMinutes(5)))
            .withCacheConfiguration("privateRestaurantById", base.entryTtl(Duration.ofMinutes(5)))

            .withCacheConfiguration("publicDishById", base.entryTtl(Duration.ofMinutes(5)))
            .withCacheConfiguration("privateDishById", base.entryTtl(Duration.ofMinutes(5)))
            .withCacheConfiguration("publicDishesByRestaurantId", base.entryTtl(Duration.ofMinutes(2)))
            .withCacheConfiguration("privateDishesByRestaurantId", base.entryTtl(Duration.ofMinutes(2)))

            .withCacheConfiguration("publicTableById", base.entryTtl(Duration.ofMinutes(5)))
            .withCacheConfiguration("privateTableById", base.entryTtl(Duration.ofMinutes(5)))
            .withCacheConfiguration("publicTablesByRestaurantId", base.entryTtl(Duration.ofMinutes(2)))
            .withCacheConfiguration("privateTablesByRestaurantId", base.entryTtl(Duration.ofMinutes(2)))
            .withCacheConfiguration("restaurantBookingTable", base.entryTtl(Duration.ofMinutes(2)))

            .withCacheConfiguration("photosByDishId", base.entryTtl(Duration.ofMinutes(1)))
            .withCacheConfiguration("photosByRestaurantId", base.entryTtl(Duration.ofMinutes(1)))

            .withCacheConfiguration("managerHasAccess", base.entryTtl(Duration.ofMinutes(30)))
            .withCacheConfiguration("restaurantManagers", base.entryTtl(Duration.ofMinutes(10)));
    }
}