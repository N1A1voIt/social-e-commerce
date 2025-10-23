package com.itu.socialcom.demo.posts.repository;

import com.itu.socialcom.demo.posts.entity.LikesStateLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesStateLogRepository extends JpaRepository<LikesStateLog, Long> {
    Iterable<LikesStateLog> findByUsername(String username);
}
