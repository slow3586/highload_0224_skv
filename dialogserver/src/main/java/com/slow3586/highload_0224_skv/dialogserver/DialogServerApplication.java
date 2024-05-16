package com.slow3586.highload_0224_skv.dialogserver;

import com.slow3586.highload_0224_skv.commonapi.MongoUuidEntity;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.mapping.event.ReactiveBeforeConvertCallback;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@SpringBootApplication
@EnableCaching
@EnableReactiveMongoRepositories
public class DialogServerApplication {

    @NonFinal
    @Value("${enable-cache:true}")
    boolean enableCache;

    public static void main(String[] args) {
        SpringApplication.run(DialogServerApplication.class, args);
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(60))
            .disableCachingNullValues()
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(
                    new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public ReactiveBeforeConvertCallback<MongoUuidEntity> beforeSaveCallback() {
        return (entity, collection) ->
            Mono.fromCallable(() -> {
                if (entity.getId() == null) {
                    entity.setId(UUID.randomUUID());
                }
                return entity;
            });
    }
}
