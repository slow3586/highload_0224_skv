package com.slow3586.highload_0224_skv.mainapp.security;

import com.slow3586.highload_0224_skv.mainapp.exception.UserNotFoundException;
import com.slow3586.highload_0224_skv.mainapp.repository.read.UserReadRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;

import java.util.Collections;
import java.util.UUID;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class ReactiveSecurityWebFilterChainConfig {
    ReactiveAuthenticationWebFilter reactiveAuthenticationWebFilter;
    UserReadRepository userReadRepository;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
        ServerHttpSecurity http,
        ReactiveAuthenticationManager reactiveAuthenticationManager
    ) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(ServerHttpSecurity.CorsSpec::disable)
            .authorizeExchange(request ->
                request.pathMatchers(
                        "",
                        "/login",
                        "/webjars/swagger-ui/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/health"
                    ).permitAll()
                    .anyExchange().authenticated()
            ).authenticationManager(reactiveAuthenticationManager)
            .securityContextRepository(new WebSessionServerSecurityContextRepository())
            .addFilterAt(reactiveAuthenticationWebFilter, SecurityWebFiltersOrder.HTTP_BASIC)
            .build();
    }

    public UserDetails loadUserByUsername(String uuidString) {
        return userReadRepository.findById(UUID.fromString(uuidString))
            .map(u -> new User(
                String.valueOf(u.getId()),
                u.getPassword(),
                Collections.emptyList()))
            .orElseThrow(UserNotFoundException::new);
    }
}
