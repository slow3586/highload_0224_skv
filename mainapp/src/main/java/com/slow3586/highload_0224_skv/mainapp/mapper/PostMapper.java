package com.slow3586.highload_0224_skv.mainapp.mapper;

import com.slow3586.highload_0224_skv.api.model.Post;
import com.slow3586.highload_0224_skv.mainapp.entity.PostEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostMapper {
    Post postEntityToPost(PostEntity userEntity);
    PostEntity postToPostEntity(Post user);
}
