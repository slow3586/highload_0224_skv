package com.slow3586.highload_0224_skv.websocketserver;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class FriendshipService {
    WebSocketServerApiClient webSocketServerApiClient;

    @Cacheable(value = "FriendCacheService#getActiveUserFriends")
    public Mono<Boolean> checkIfUsersAreFriends(String user0, String user1) {
        return webSocketServerApiClient.checkIfUsersAreFriends(user0, user1);
    }

    @CacheEvict(value = "FriendCacheService#getActiveUserFriends", allEntries = true)
    @Scheduled(fixedRate = 100_000)
    public void emptyFriendsCache() {
    }
}
