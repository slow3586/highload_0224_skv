package com.slow3586.highload_0224_skv.dialogserver;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Hooks;

public class DialogServiceConfig {
    @PostConstruct
    public void enable() {
        Hooks.enableAutomaticContextPropagation();
    }
}
