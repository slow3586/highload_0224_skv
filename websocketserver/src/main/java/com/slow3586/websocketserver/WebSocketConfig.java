package com.slow3586.websocketserver;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Optional;

@Configuration
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    WebSocketServerApiClient webSocketServerApiClient;
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
        registration.interceptors(new MyChannelInterceptor());
    }

    protected class MyChannelInterceptor implements ChannelInterceptor {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            final StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                Optional.ofNullable(accessor.getNativeHeader("Authorization"))
                    .filter(l -> !l.isEmpty())
                    .ifPresent(ah -> {
                        final String jwt = ah.get(0).substring("Bearer ".length());
                        final String username = webSocketServerApiClient.getUser(jwt);
                        accessor.setUser(() -> username);
                    });
            } else {
                throw new IllegalArgumentException("Could not authenticate user.");
            }
            return message;
        }
    }
}
