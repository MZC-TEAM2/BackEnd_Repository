package com.mzc.backend.lms.domains.notification.queue.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 알림 시스템용 Redis 설정
 */
@Configuration
public class NotificationRedisConfig {

    public static final String NOTIFICATION_QUEUE_KEY = "notification:queue";
    public static final String BATCH_NOTIFICATION_QUEUE_KEY = "notification:batch:queue";
    public static final String NOTIFICATION_PROCESSING_KEY = "notification:processing";

    /**
     * Redis 전용 ObjectMapper 생성 (Bean으로 등록하지 않음)
     * HTTP ObjectMapper와 분리하기 위해 내부 메서드로 사용
     */
    private ObjectMapper createNotificationObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 타입 정보를 JSON에 포함하여 역직렬화 시 올바른 타입으로 복원
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return objectMapper;
    }

    @Bean(name = "notificationRedisTemplate")
    public RedisTemplate<String, Object> notificationRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key는 String, Value는 JSON 직렬화 (Redis 전용 ObjectMapper 사용)
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(createNotificationObjectMapper());

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
