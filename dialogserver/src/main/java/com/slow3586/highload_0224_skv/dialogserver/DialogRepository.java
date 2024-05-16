package com.slow3586.highload_0224_skv.dialogserver;

import com.slow3586.highload_0224_skv.commonapi.DialogEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface DialogRepository extends ReactiveMongoRepository<DialogEntity, UUID> {
    Mono<DialogEntity> findByUser0AndUser1(UUID user0, UUID user1);
}
