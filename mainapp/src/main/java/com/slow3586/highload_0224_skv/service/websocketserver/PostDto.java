package com.slow3586.highload_0224_skv.service.websocketserver;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
class PostDto implements Serializable {
    @JsonProperty("postId")
    String postId;
    @JsonProperty("postText")
    String postText;
    @JsonProperty("userId")
    String userId;
}
