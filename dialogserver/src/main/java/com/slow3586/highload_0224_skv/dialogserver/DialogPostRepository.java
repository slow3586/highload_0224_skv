package com.slow3586.highload_0224_skv.dialogserver;

import com.slow3586.highload_0224_skv.commonapi.DialogPostEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface DialogPostRepository extends ReactiveMongoRepository<DialogPostEntity, UUID> {
    Flux<DialogPostEntity> findByDialogId(UUID dialogId);
}
