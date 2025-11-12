package com.dancing.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类 - 用于视频通话信令
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单的消息代理，用于向客户端发送消息
        config.enableSimpleBroker("/topic", "/queue");
        // 设置客户端发送消息的前缀
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP端点，客户端通过这个端点连接
        registry.addEndpoint("/ws/video-call")
                .setAllowedOriginPatterns("*") // 允许所有来源（生产环境应限制）
                .withSockJS(); // 启用SockJS支持，提供降级方案
    }
}

