package com.slow3586.highload_0224_skv.mainapp.repository.read;

import com.slow3586.highload_0224_skv.mainapp.entity.FriendshipEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FriendshipReadRepository extends CrudRepository<FriendshipEntity, UUID> {
    List<FriendshipEntity> findAllByUserId(UUID userId);
    List<FriendshipEntity> findAllByFriendId(UUID friendId);
}
