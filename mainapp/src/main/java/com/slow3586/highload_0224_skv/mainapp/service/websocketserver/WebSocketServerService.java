package com.slow3586.highload_0224_skv.mainapp.service.websocketserver;

import com.slow3586.highload_0224_skv.commonapi.PostDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
@Service
public class WebSocketServerService {

    RabbitTemplate rabbitTemplate;
    @NonFinal
    @Value("${disable-websocket-app:false}")
    boolean disableWebsocketApp;

    public void sendPost(UUID id, UUID userId, String text) {
        if (!disableWebsocketApp) {
            rabbitTemplate.convertSendAndReceive(
                "feed",
                new PostDto(
                    id.toString(),
                    text,
                    userId.toString()));
        }
    }

}
