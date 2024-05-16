package com.slow3586.highload_0224_skv.websocketserver;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    WebSocketInterceptor webSocketInterceptor;
    @NonFinal
    @Value("${spring.rabbitmq.host:localhost}")
    String rabbitHost;
    @NonFinal
    @Value("${spring.rabbitmq.username:admin}")
    String rabbitUsername;
    @NonFinal
    @Value("${spring.rabbitmq.password:admin}")
    String rabbitPw;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config
            .setUserDestinationPrefix("/user")
            .enableStompBrokerRelay("/queue")
            .setRelayHost(rabbitHost)
            .setRelayPort(61613)
            .setClientLogin(rabbitUsername)
            .setSystemLogin(rabbitUsername)
            .setSystemPasscode(rabbitPw)
            .setClientPasscode(rabbitPw);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            //.setHandshakeHandler(webSocketHandshakeHandler)
            .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketInterceptor);
    }

}
