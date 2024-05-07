package com.slow3586.highload_0224_skv.service;

import com.slow3586.highload_0224_skv.entity.FriendshipEntity;
import com.slow3586.highload_0224_skv.repository.read.FriendshipReadRepository;
import com.slow3586.highload_0224_skv.repository.write.FriendshipWriteRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class FriendService {

    FriendshipReadRepository friendshipReadRepository;
    FriendshipWriteRepository friendshipWriteRepository;

    public void createFriendship(UUID userId, UUID friendId) {
        final FriendshipEntity friendshipEntity = new FriendshipEntity();
        friendshipEntity.setUserId(userId);
        friendshipEntity.setFriendId(friendId);
        friendshipWriteRepository.save(friendshipEntity);
    }

    public boolean checkIfUsersAreFriends(UUID user0, UUID user1) {
        return friendshipReadRepository.findAllByUserId(user0)
            .stream()
            .anyMatch(e -> e.getFriendId().equals(user1));
    }
}
