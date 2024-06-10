package com.slow3586.highload_0224_skv.mainapp;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

@Configuration
public class WebFluxConfig {
    @PostConstruct
    public void enable() {
        Hooks.enableAutomaticContextPropagation();
    }
}
