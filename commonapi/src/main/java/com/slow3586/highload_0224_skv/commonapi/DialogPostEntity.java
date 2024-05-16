package com.slow3586.highload_0224_skv.commonapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("dialog_posts")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DialogPostEntity implements MongoUuidEntity{
    UUID id;
    UUID dialogId;
    String text;
}
