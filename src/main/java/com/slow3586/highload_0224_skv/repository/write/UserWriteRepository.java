package com.slow3586.highload_0224_skv.repository.write;

import com.slow3586.highload_0224_skv.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserWriteRepository extends CrudRepository<UserEntity, UUID> {
}
