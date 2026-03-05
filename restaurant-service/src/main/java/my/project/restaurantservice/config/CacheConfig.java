package my.project.restaurantservice.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class CacheConfig {


    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        om.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return om;
    }


    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper redisObjectMapper) {
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );
    }


    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(RedisCacheConfiguration base) {
        return builder -> builder
                .cacheDefaults(base.entryTtl(Duration.ofMinutes(10))) // дефолт
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

                .withCacheConfiguration("managerAccess", base.entryTtl(Duration.ofMinutes(30)));
    }

}