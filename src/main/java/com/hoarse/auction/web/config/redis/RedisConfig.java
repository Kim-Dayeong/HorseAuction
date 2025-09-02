package com.hoarse.auction.web.config.redis;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    private final RedisConnectionFactory redisConnectionFactory;
    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    public RedisConfig(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
//        config.setPassword(password);
        return new LettuceConnectionFactory(config);
    }


    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }
    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        return new StringRedisTemplate(redisConnectionFactory);
    }
//    //리스너 어댑터 설정
//    @Bean
//    MessageListenerAdapter messageListenerAdapter() {
//        return new MessageListenerAdapter(new RedisSubService());
//    }
//
//    //컨테이너 설정
//    @Bean
//    RedisMessageListenerContainer redisContainer() {
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(redisConnectionFactory());
//        container.addMessageListener(messageListenerAdapter(), topic());
//        return container;
//    }
//
//    //pub/sub 토픽 설정
//    @Bean
//    ChannelTopic topic() {
//        return new ChannelTopic("topic1");
//    }
}

