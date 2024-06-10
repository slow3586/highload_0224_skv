package com.slow3586.highload_0224_skv.dialogserver;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

@Configuration
public class DialogServiceConfig {
    @PostConstruct
    public void enable() {
        Hooks.enableAutomaticContextPropagation();
    }
}
