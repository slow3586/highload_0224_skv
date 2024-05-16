package com.slow3586.highload_0224_skv.mainapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@Table("posts")
@NoArgsConstructor
@AllArgsConstructor
public class PostEntity {
    @Id UUID id;
    UUID authorUserId;
    Date dateCreated;
    String text;
}
