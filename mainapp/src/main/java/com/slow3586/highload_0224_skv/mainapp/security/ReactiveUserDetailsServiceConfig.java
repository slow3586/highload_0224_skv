package com.slow3586.highload_0224_skv.mainapp.security;

import com.slow3586.highload_0224_skv.mainapp.repository.read.UserReadRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Configuration
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class ReactiveUserDetailsServiceConfig {
    UserReadRepository userReadRepository;

    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService() {
        return (String username) ->
            userReadRepository.findById(UUID.fromString(username))
                .map(u -> new User(
                    u.getId().toString(),
                    u.getPassword(),
                    List.of()))
                .map(Mono::just)
                .orElseThrow()
                .cast(UserDetails.class);
    }
}
