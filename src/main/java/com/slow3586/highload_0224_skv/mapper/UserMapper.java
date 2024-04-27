package com.slow3586.highload_0224_skv.mapper;

import com.slow3586.highload_0224_skv.api.model.User;
import com.slow3586.highload_0224_skv.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User userEntityToUser(UserEntity userEntity);
    UserEntity userToUserEntity(User user);
}
