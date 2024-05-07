package com.slow3586.websocketserver;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class RabbitQueueCreator {
    AmqpAdmin amqpAdmin;

    @PostConstruct
    public void createQueues() {
        amqpAdmin.declareQueue(new Queue("feed", true));
    }
}
