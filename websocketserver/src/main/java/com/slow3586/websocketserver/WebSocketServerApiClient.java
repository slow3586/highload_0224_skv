package com.slow3586.websocketserver;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class WebSocketServerApiClient {
    WebClient.Builder webClientBuilder;
    @NonFinal
    @Value("${main-app-url:http://localhost:8080}")
    String mainAppUrl;

    public Mono<Boolean> checkIfUsersAreFriends(String user0, String user1) {
        return webClientBuilder
            .baseUrl(mainAppUrl)
            .build()
            .get()
            .uri(builder -> builder.path("/checkIfUsersAreFriends")
                .queryParam("user0", user0)
                .queryParam("user1", user1)
                .build()
            ).retrieve()
            .bodyToMono(Boolean.class);
    }

    public String getUser(String jwt) {
        return webClientBuilder
            .baseUrl(mainAppUrl)
            .build()
            .post()
            .uri("/getUser")
            .bodyValue(jwt)
            .retrieve()
            .bodyToMono(String.class)
            .block(Duration.ofSeconds(5));
    }
}
