package com.slow3586.highload_0224_skv.commonapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("dialogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DialogEntity implements MongoUuidEntity {
    @Id UUID id;
    UUID user0;
    UUID user1;
}
