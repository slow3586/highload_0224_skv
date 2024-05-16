package com.slow3586.highload_0224_skv.dialogserver;

import com.slow3586.highload_0224_skv.commonapi.DialogEntity;
import com.slow3586.highload_0224_skv.commonapi.DialogPostEntity;
import com.slow3586.highload_0224_skv.commonapi.SendDialogPostDto;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class DialogService {
    static final String DIALOG_SERVICE_GET_DIALOG = "DialogService#getDialog";
    static final String DIALOG_SERVICE_GET_DIALOG_POSTS = "DialogService#getDialogPosts";
    static final RedisScript<String> REDIS_SCRIPT_GET_DIALOG =
        RedisScript.of("return redis.call('GET', KEYS[1])", String.class);
    static final RedisScript<Object> REDIS_SCRIPT_INSERT_DIALOG =
        RedisScript.of("redis.call('SET', KEYS[1], ARGV[1])");
    DialogRepository dialogRepository;
    DialogPostRepository dialogPostRepository;
    CacheManager cacheManager;
    RedisOperations<String, String> redisOperations;
    @NonFinal
    @Value("${enable-cache:true}")
    boolean enableCache;

    public Mono<UUID> getDialog(
        @NonNull final UUID user0,
        @NonNull final UUID user1
    ) {
        if (user0.equals(user1)) throw new IllegalArgumentException("Same user");

        final List<UUID> ids = this.sortUUIDs(user0, user1);
        final List<String> keys = List.of(DIALOG_SERVICE_GET_DIALOG + "::" + user0 + "_" + user1);

        return Mono.justOrEmpty(enableCache
                ? redisOperations.execute(REDIS_SCRIPT_GET_DIALOG, keys)
                : null
            ).mapNotNull(UUID::fromString)
            .switchIfEmpty(dialogRepository.findByUser0AndUser1(
                    ids.get(0),
                    ids.get(1))
                .map(DialogEntity::getId)
                .doOnNext(uuid -> {
                    if (enableCache) {
                        redisOperations.execute(REDIS_SCRIPT_INSERT_DIALOG, keys, uuid.toString());
                    }
                }));
    }

    public Flux<DialogPostEntity> getDialogPosts(@NonNull final UUID dialogId) {
        return Mono.justOrEmpty(enableCache
                ? cacheManager.getCache(DIALOG_SERVICE_GET_DIALOG_POSTS)
                : null
            ).mapNotNull(c -> c.get(dialogId))
            .mapNotNull(Cache.ValueWrapper::get)
            .switchIfEmpty(Mono.just(dialogId)
                .filterWhen(dialogRepository::existsById)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No dialog found with id: " + dialogId)))
                .flatMapMany(dialogPostRepository::findByDialogId)
                .collectList()
                .doOnNext((list) -> {
                    if (enableCache) {
                        cacheManager.getCache(DIALOG_SERVICE_GET_DIALOG_POSTS)
                            .put(dialogId, list);
                    }
                }))
            .flatMapMany(list -> Flux.fromIterable(((List<DialogPostEntity>) list)));
    }

    public Mono<Void> sendDialogPost(@NonNull final SendDialogPostDto dto) {
        return Mono.fromCallable(() ->
                this.sortUUIDs(dto.getAuthorUserId(), dto.getReceiverUserId())
            ).flatMap(uuids -> this.getDialog(
                    uuids.get(0),
                    uuids.get(1))
                .switchIfEmpty(dialogRepository.save(
                    DialogEntity.builder()
                        .user0(uuids.get(0))
                        .user1(uuids.get(1))
                        .build()).map(DialogEntity::getId))
            ).map(dialogId -> DialogPostEntity.builder()
                .dialogId(dialogId)
                .text(dto.getText())
                .build())
            .flatMap(dialogPostRepository::save)
            .doOnNext((dialogPostEntity) -> {
                if (enableCache) {
                    cacheManager.getCache(DIALOG_SERVICE_GET_DIALOG_POSTS)
                        .evictIfPresent(dialogPostEntity.getDialogId());
                }
            }).then();
    }

    protected List<UUID> sortUUIDs(UUID uuid0, UUID uuid1) {
        final TreeSet<String> ids = new TreeSet<>();
        ids.add(uuid0.toString());
        ids.add(uuid1.toString());
        return ids.stream().map(UUID::fromString).toList();
    }
}
