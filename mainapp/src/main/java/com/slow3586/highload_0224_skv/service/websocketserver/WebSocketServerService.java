package com.slow3586.highload_0224_skv.service.websocketserver;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
@Service
public class WebSocketServerService {

    RabbitTemplate rabbitTemplate;

    public void sendPost(UUID id, UUID userId, String text) {
        rabbitTemplate.convertSendAndReceive(
            "feed",
            new PostDto(
                id.toString(),
                text,
                userId.toString()));
    }

}
