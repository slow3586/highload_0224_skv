package com.slow3586.highload_0224_skv.mainapp.repository.write;

import com.slow3586.highload_0224_skv.mainapp.entity.PostEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostWriteRepository extends CrudRepository<PostEntity, UUID> {
}
