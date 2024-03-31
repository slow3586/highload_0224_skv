package com.slow3586.highload_0224_skv.repository;

import com.slow3586.highload_0224_skv.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, UUID> {

    List<UserEntity> searchAllByFirstNameContainingAndSecondNameContaining(
        @Param("first_name") String firstName, @Param("second_name") String lastName);
}
