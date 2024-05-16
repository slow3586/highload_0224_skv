package com.slow3586.highload_0224_skv.websocketserver;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class WebSocketInterceptor implements ChannelInterceptor {
    WebSocketServerApiClient webSocketServerApiClient;

    @Override
    public Message<?> preSend(
        final Message<?> message,
        final MessageChannel channel
    ) {
        final StompHeaderAccessor headerAccessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (headerAccessor != null
            && StompCommand.CONNECT.equals(headerAccessor.getCommand())
        ) {
            final String txt = Optional.ofNullable(
                    headerAccessor.getNativeHeader("Authorization")
                ).filter(l -> !l.isEmpty())
                .map(s -> s.get(0))
                .filter(s -> s.contains("Bearer "))
                .map(s -> s.substring("Bearer ".length()))
                .map(webSocketServerApiClient::getUser)
                .orElseThrow(() -> new IllegalArgumentException("Could not authenticate user"));
            headerAccessor.setUser(() -> txt);
        }

        return message;
    }
}
