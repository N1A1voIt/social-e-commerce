package com.itu.socialcom.demo.posts.repository;

import com.itu.socialcom.demo.posts.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Integer> {

    List<Media> findByIdChild(Integer idChild);
}
