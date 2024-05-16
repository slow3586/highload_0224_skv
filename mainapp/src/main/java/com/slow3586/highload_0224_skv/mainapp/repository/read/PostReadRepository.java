package com.slow3586.highload_0224_skv.mainapp.repository.read;

import com.slow3586.highload_0224_skv.mainapp.entity.PostEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostReadRepository extends PagingAndSortingRepository<PostEntity, UUID> {
    List<PostEntity> findAllByAuthorUserId(UUID authorId, Pageable pageable);
}
