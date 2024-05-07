package com.slow3586.highload_0224_skv.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@Table("friendships")
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipEntity {
    @Id UUID id;
    UUID userId;
    UUID friendId;
}
