package com.slow3586.highload_0224_skv.service;

import com.slow3586.highload_0224_skv.api.model.Post;
import com.slow3586.highload_0224_skv.entity.PostEntity;
import com.slow3586.highload_0224_skv.mapper.PostMapper;
import com.slow3586.highload_0224_skv.repository.read.FriendshipReadRepository;
import com.slow3586.highload_0224_skv.repository.read.PostReadRepository;
import com.slow3586.highload_0224_skv.repository.write.FriendshipWriteRepository;
import com.slow3586.highload_0224_skv.repository.write.PostWriteRepository;
import com.slow3586.highload_0224_skv.service.websocketserver.WebSocketServerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.postgresql.PGConnection;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class PostService {
    PostReadRepository postReadRepository;
    PostWriteRepository postWriteRepository;
    FriendshipReadRepository friendshipReadRepository;
    FriendshipWriteRepository friendshipWriteRepository;
    PostMapper postMapper;
    JdbcTemplate jdbcTemplate;
    CacheManager cacheManager;
    WebSocketServerService webSocketServerService;

    @Cacheable(value = "postsByFriends", key = "#userId", condition = "#offset == 0 && #limit == 10")
    public List<Post> findPostsByFriends(UUID userId, int offset, int limit) {
        return jdbcTemplate.queryForList(
                """
                    SELECT * FROM POSTS
                    LEFT JOIN FRIENDSHIPS ON friendships.user_id = ?
                    WHERE posts.author_user_id = friendships.friend_id
                    ORDER BY posts.date_created DESC
                    OFFSET ?
                    LIMIT ?""",
                userId, offset, limit)
            .stream()
            .map(e -> {
                Post post = new Post();
                post.setId(e.get("id").toString());
                post.setAuthorUserId(e.get("author_user_id").toString());
                post.setText(e.get("text").toString());
                return post;
            }).toList();
    }

    public UUID createPost(UUID userId, String text) {
        final PostEntity entity = new PostEntity();
        entity.setId(null);
        entity.setAuthorUserId(userId);
        entity.setText(text);
        entity.setDateCreated(new Date());
        final UUID id = postWriteRepository.save(entity).getId();

        webSocketServerService.sendPost(id, userId, text);

        return id;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
    public void listener() {
        jdbcTemplate.execute((Connection connection) -> {
            connection.createStatement().execute("LISTEN \"POSTS_UPDATE\"");
            Arrays.stream(connection
                .unwrap(PGConnection.class)
                .getNotifications(999)
            ).forEach(nt -> friendshipWriteRepository
                .findAllByFriendId(UUID.fromString(nt.getParameter()))
                .forEach(friendship -> cacheManager.getCache("postsByFriends").evictIfPresent(friendship.getUserId())));
            return null;
        });
    }

}
