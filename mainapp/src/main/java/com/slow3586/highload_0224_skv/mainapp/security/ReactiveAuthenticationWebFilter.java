package com.slow3586.highload_0224_skv.mainapp.security;

import com.slow3586.highload_0224_skv.mainapp.repository.read.UserReadRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class ReactiveAuthenticationWebFilter implements WebFilter {
    static String BEARER_PREFIX = "Bearer ";
    static String HEADER_NAME = "Authorization";
    JwtService jwtService;
    UserReadRepository userReadRepository;
    ReactiveAuthenticationManager reactiveAuthenticationManager;

    @Override
    public Mono<Void> filter(
        final ServerWebExchange exchange,
        final WebFilterChain chain
    ) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().get(HEADER_NAME))
            .filter(l -> !l.isEmpty())
            .mapNotNull(l -> l.get(0))
            .filter(s -> s.startsWith(BEARER_PREFIX))
            .mapNotNull(s -> s.substring(BEARER_PREFIX.length()))
            .filter(s -> !s.isBlank())
            .mapNotNull(jwtService::extractUserId)
            .filter(userReadRepository::existsById)
            .map(uuid -> new UsernamePasswordAuthenticationToken(
                new User(uuid.toString(), "",
                    AuthorityUtils.createAuthorityList("test")),
                null,
                AuthorityUtils.createAuthorityList("test")))
            .singleOptional()
            .publishOn(Schedulers.boundedElastic())
            .flatMap(option ->
                option.map(auth -> chain
                    .filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
                ).orElseGet(() -> chain.filter(exchange)));
    }
}
