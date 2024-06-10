package com.slow3586.highload_0224_skv.mainapp.service;

import com.slow3586.highload_0224_skv.api.model.DialogMessage;
import com.slow3586.highload_0224_skv.commonapi.DialogPostEntity;
import com.slow3586.highload_0224_skv.commonapi.SendDialogPostDto;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class DialogService {
    WebClient.Builder webClientBuilder;
    @NonFinal
    @Value("${dialog-app-url:http://localhost:8083}")
    String dialogAppUrl;

    public Flux<DialogMessage> getDialogPosts(
        @NonNull final UUID currentUserId,
        @NonNull final UUID userId
    ) {
        log.info("#getDialogPosts started: {} {}", currentUserId, userId);
        return getClient()
            .get()
            .uri(builder ->
                builder.path("/getDialog")
                    .queryParam("user0", currentUserId)
                    .queryParam("user1", userId)
                    .build())
            .retrieve()
            .bodyToMono(UUID.class)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("No dialog")))
            .doOnNext(dialogId -> log.info("#getDialogPosts got dialogId: {} {} {}", currentUserId, userId, dialogId))
            .flatMapMany(dialogId ->
                getClient()
                    .get()
                    .uri(builder ->
                        builder.path("/getDialogPosts")
                            .queryParam("dialogId", dialogId)
                            .build())
                    .retrieve()
                    .bodyToFlux(DialogPostEntity.class))
            .map(dialogPostEntity -> new DialogMessage(
                currentUserId.toString(),
                userId.toString(),
                dialogPostEntity.getText()))
            .doOnNext(msg -> log.info("#getDialogPosts got dialogMessage: {} {} {}", currentUserId, userId, msg));
    }

    public Mono<Void> sendDialogPost(
        @NonNull final UUID authorUserId,
        @NonNull final UUID receiverUserId,
        @NonNull final String text
    ) {
        return getClient()
            .post()
            .uri("/sendDialogPost")
            .bodyValue(new SendDialogPostDto(
                authorUserId,
                receiverUserId,
                text
            )).retrieve()
            .bodyToMono(Void.class);
    }

    protected WebClient getClient() {
        return webClientBuilder.baseUrl(dialogAppUrl).build();
    }
}
