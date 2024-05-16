package com.slow3586.highload_0224_skv.dialogserver;

import com.slow3586.highload_0224_skv.commonapi.DialogPostEntity;
import com.slow3586.highload_0224_skv.commonapi.SendDialogPostDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class DialogServiceRest {
    DialogService dialogService;

    @GetMapping(path = "/getDialog")
    public Mono<UUID> getDialog(
        @RequestParam final UUID user0,
        @RequestParam final UUID user1
    ) {
        return dialogService.getDialog(user0, user1);
    }

    @GetMapping(path = "/getDialogPosts")
    public Flux<DialogPostEntity> getDialogPosts(
        @RequestParam UUID dialogId
    ) {
        return dialogService.getDialogPosts(dialogId);
    }

    @PostMapping(path = "/sendDialogPost")
    public Mono<Void> sendDialogPost(
        @RequestBody SendDialogPostDto sendDialogDto
    ) {
        return dialogService.sendDialogPost(sendDialogDto);
    }

}
