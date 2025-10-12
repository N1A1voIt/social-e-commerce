package com.itu.socialcom.demo.posts.repository;

import com.itu.socialcom.demo.posts.entity.PostChild;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface PostChildRepository extends JpaRepository<PostChild, Integer> {
    List<PostChild> findByPlatformIdentifier(String platformIdentifier);
    @Query("SELECT DISTINCT p.platformIdentifier FROM PostChild p WHERE p.idSp = :idSp")
    Set<String> findDistinctPlatformIdentifierByIdSp(@Param("idSp") Long idSp);
    List<PostChild> findByIdSp(Long idSp);
    List<PostChild> findByIdPost(Integer idPost);
}
