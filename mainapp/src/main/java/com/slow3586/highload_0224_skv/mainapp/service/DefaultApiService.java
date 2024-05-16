package com.slow3586.highload_0224_skv.mainapp.service;

import com.slow3586.highload_0224_skv.api.DefaultApiDelegate;
import com.slow3586.highload_0224_skv.api.model.DialogMessage;
import com.slow3586.highload_0224_skv.api.model.DialogUserIdSendPostRequest;
import com.slow3586.highload_0224_skv.api.model.LoginPost200Response;
import com.slow3586.highload_0224_skv.api.model.LoginPostRequest;
import com.slow3586.highload_0224_skv.api.model.Post;
import com.slow3586.highload_0224_skv.api.model.PostCreatePostRequest;
import com.slow3586.highload_0224_skv.api.model.User;
import com.slow3586.highload_0224_skv.api.model.UserRegisterPost200Response;
import com.slow3586.highload_0224_skv.api.model.UserRegisterPostRequest;
import com.slow3586.highload_0224_skv.mainapp.entity.UserEntity;
import com.slow3586.highload_0224_skv.mainapp.exception.IncorrectLoginException;
import com.slow3586.highload_0224_skv.mainapp.exception.UserNotFoundException;
import com.slow3586.highload_0224_skv.mainapp.mapper.PostMapper;
import com.slow3586.highload_0224_skv.mainapp.mapper.UserMapper;
import com.slow3586.highload_0224_skv.mainapp.repository.read.UserReadRepository;
import com.slow3586.highload_0224_skv.mainapp.repository.write.UserWriteRepository;
import com.slow3586.highload_0224_skv.mainapp.security.JwtService;
import com.slow3586.highload_0224_skv.mainapp.security.ReactiveSecurityWebFilterChainConfig;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class DefaultApiService implements DefaultApiDelegate {
    PasswordService passwordService;
    UserReadRepository userReadRepository;
    UserWriteRepository userWriteRepository;
    PostService postService;
    FriendService friendService;
    ReactiveAuthenticationManager reactiveAuthenticationManager;
    ReactiveSecurityWebFilterChainConfig reactiveSecurityWebFilterChainConfig;
    PostMapper postMapper;
    UserMapper userMapper;
    JwtService jwtService;
    DialogService dialogService;

    @Override
    public Mono<ResponseEntity<LoginPost200Response>> loginPost(
        final Mono<LoginPostRequest> loginPostRequest,
        final ServerWebExchange serverWebExchange
    ) {
        return loginPostRequest
            .filterWhen(request -> reactiveAuthenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                        request.getId(),
                        request.getPassword()))
                .filter(Authentication::isAuthenticated)
                .hasElement())
            .mapNotNull(LoginPostRequest::getId)
            .mapNotNull(reactiveSecurityWebFilterChainConfig::loadUserByUsername)
            .mapNotNull(jwtService::generateToken)
            .mapNotNull(LoginPost200Response::new)
            .mapNotNull(ResponseEntity::ok)
            .switchIfEmpty(Mono.error(new IllegalArgumentException()));
    }

    @Override
    public Mono<ResponseEntity<UserRegisterPost200Response>> userRegisterPost(
        final Mono<UserRegisterPostRequest> userRegisterPostRequest,
        final ServerWebExchange serverWebExchange
    ) {
        return userRegisterPostRequest.map(request ->
            ResponseEntity.ok(
                UserRegisterPost200Response.builder()
                    .userId(userWriteRepository.save(
                        UserEntity.builder()
                            .firstName(request.getFirstName())
                            .secondName(request.getSecondName())
                            .biography(request.getBiography())
                            .birthdate(request.getBirthdate())
                            .city(request.getCity())
                            .password(passwordService.encode(request.getPassword()))
                            .build()).getId().toString())
                    .build()));
    }

    @Override
    public Mono<ResponseEntity<User>> userGetIdGet(
        final String id,
        final ServerWebExchange exchange
    ) {
        return Mono.just(
            ResponseEntity.ok(
                userMapper.userEntityToUser(
                    this.findUser(id))));
    }

    @Override
    public Mono<ResponseEntity<Flux<User>>> userSearchGet(
        final String firstName,
        final String lastName,
        final ServerWebExchange exchange
    ) {
        return Mono.just(
            ResponseEntity.ok(
                Flux.fromIterable(
                    userReadRepository.searchAllByFirstNameContainingAndSecondNameContaining(
                            firstName,
                            lastName
                        ).stream()
                        .map(userMapper::userEntityToUser)
                        .toList())));
    }

    @Override
    public Mono<ResponseEntity<Void>> friendSetUserIdPut(
        final String userId,
        final ServerWebExchange exchange
    ) {
        //friendService.createFriendship(getCurrentUserId(), UUID.fromString(userId));
        return Mono.just(ResponseEntity.ok().build());
    }

    @Override
    public Mono<ResponseEntity<Flux<Post>>> postFeedGet(
        final BigDecimal offset,
        final BigDecimal limit,
        final ServerWebExchange exchange
    ) {
        if (offset == null || limit == null) throw new IllegalArgumentException("offset == null || limit == null");
        if (limit.intValue() <= 0 || limit.intValue() > 1000) throw new IllegalArgumentException("limit <=0 || limit > 1000");
        if (offset.intValue() < 0) throw new IllegalArgumentException("offset < 0");
        return Mono.just(
            ResponseEntity.ok(
                Flux.fromIterable(
                    postService.findPostsByFriends(
                        null,
                        offset.intValue(),
                        limit.intValue()))));
    }

    @Override
    public Mono<ResponseEntity<String>> postCreatePost(
        final Mono<PostCreatePostRequest> postCreatePostRequest,
        final ServerWebExchange exchange
    ) {
        return postCreatePostRequest.map(request ->
            ResponseEntity.ok(
                postService.createPost(
                        null,
                        request.getText())
                    .toString()));
    }

    @Override
    public Mono<ResponseEntity<Flux<DialogMessage>>> dialogUserIdListGet(
        final String userId,
        final ServerWebExchange exchange
    ) {
        return getCurrentUserId()
            .map(currentUserId ->
                ResponseEntity.ok(
                    dialogService.getDialogPosts(
                        currentUserId,
                        UUID.fromString(userId))));
    }

    @Override
    public Mono<ResponseEntity<Void>> dialogUserIdSendPost(
        final String receiverUserId,
        final Mono<DialogUserIdSendPostRequest> dialogUserIdSendPostRequest,
        final ServerWebExchange exchange
    ) {
        return Mono.zip(
            getCurrentUserId(),
            dialogUserIdSendPostRequest.map(DialogUserIdSendPostRequest::getText)
        ).flatMap(tuple -> dialogService.sendDialogPost(
            tuple.getT1(),
            UUID.fromString(receiverUserId),
            tuple.getT2())
        ).then(Mono.just(new ResponseEntity<>(HttpStatus.OK)));
    }

    protected Mono<UUID> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getPrincipal)
            .cast(org.springframework.security.core.userdetails.User.class)
            .map(org.springframework.security.core.userdetails.User::getUsername)
            .map(UUID::fromString)
            .switchIfEmpty(Mono.empty());
    }

    protected UserEntity findUser(final String uuidString) {
        UUID uuid;

        try {
            uuid = UUID.fromString(uuidString);
        } catch (Exception e) {
            throw new IncorrectLoginException();
        }

        return userReadRepository.findById(uuid)
            .orElseThrow(UserNotFoundException::new);
    }
}
