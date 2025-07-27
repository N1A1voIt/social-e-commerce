package com.itu.socialcom.demo.posts.repository;

import com.itu.socialcom.demo.posts.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Integer> {
}
