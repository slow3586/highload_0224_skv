package com.slow3586.highload_0224_skv.service;

import com.slow3586.highload_0224_skv.entity.FriendshipEntity;
import com.slow3586.highload_0224_skv.repository.write.FriendshipWriteRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class FriendService {

    FriendshipWriteRepository friendshipWriteRepository;

    public void createFriendship(UUID userId, UUID friendId) {
        final FriendshipEntity friendshipEntity = new FriendshipEntity();
        friendshipEntity.setUserId(userId);
        friendshipEntity.setFriendId(friendId);
        friendshipWriteRepository.save(friendshipEntity);
    }
}
