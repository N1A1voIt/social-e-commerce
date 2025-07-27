package com.itu.socialcom.demo.posts.repository;

import com.itu.socialcom.demo.posts.entity.PostChild;
import com.itu.socialcom.demo.products.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostChildRepository extends JpaRepository<PostChild, Integer> {
    Optional<PostChild> findByPlatformIdentifier(String platformIdentifier);

    List<PostChild> findByIdSp(Long idSp);
}
