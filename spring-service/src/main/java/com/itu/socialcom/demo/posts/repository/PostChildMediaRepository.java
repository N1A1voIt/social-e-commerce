package com.itu.socialcom.demo.posts.repository;

import com.itu.socialcom.demo.posts.entity.PostChildMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostChildMediaRepository extends JpaRepository<PostChildMedia, Long> {
}
