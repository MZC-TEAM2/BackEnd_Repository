package com.mzc.backend.lms.domains.attendance.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * 출석 시스템용 Redis Pub/Sub 설정
 */
@Configuration
public class AttendanceRedisConfig {

    public static final String CONTENT_COMPLETED_CHANNEL = "attendance:content-completed";

    /**
     * 출석 이벤트용 ObjectMapper
     */
    @Bean(name = "attendanceObjectMapper")
    public ObjectMapper attendanceObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    /**
     * Redis Pub/Sub 채널 토픽
     */
    @Bean(name = "contentCompletedTopic")
    public ChannelTopic contentCompletedTopic() {
        return new ChannelTopic(CONTENT_COMPLETED_CHANNEL);
    }

    /**
     * 메시지 리스너 어댑터
     */
    @Bean(name = "contentCompletedListenerAdapter")
    public MessageListenerAdapter contentCompletedListenerAdapter(
            ContentCompletedEventListener listener) {
        return new MessageListenerAdapter(listener, "onMessage");
    }

    /**
     * Redis 메시지 리스너 컨테이너
     */
    @Bean(name = "attendanceRedisListenerContainer")
    public RedisMessageListenerContainer attendanceRedisListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter contentCompletedListenerAdapter,
            ChannelTopic contentCompletedTopic) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(contentCompletedListenerAdapter, contentCompletedTopic);
        return container;
    }
}
