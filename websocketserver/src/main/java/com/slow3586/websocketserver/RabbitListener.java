package com.slow3586.websocketserver;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class RabbitListener {
    SimpMessagingTemplate simpMessagingTemplate;
    SimpUserRegistry simpUserRegistry;
    FriendshipService friendshipService;

    @org.springframework.amqp.rabbit.annotation.RabbitListener(queues = {"feed"})
    public void listener(@Payload PostDto postDto) {
        final String userId = postDto.getUserId();
        Flux.fromIterable(simpUserRegistry.getUsers())
            .map(SimpUser::getName)
            .filterWhen(u -> friendshipService.checkIfUsersAreFriends(u, userId))
            .doOnNext(u -> simpMessagingTemplate.convertAndSendToUser(
                u, "/queue/feed", postDto))
            .subscribe();
    }
}
