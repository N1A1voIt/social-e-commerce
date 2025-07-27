package com.itu.socialcom.demo.posts.repository;

import com.itu.socialcom.demo.posts.entity.VRefreshTokenHolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VRefreshTokenHolderRepository extends JpaRepository<VRefreshTokenHolder, Long> {
    List<VRefreshTokenHolder> findByIdSellerAndIdSp(Integer idSeller, Long platformId);
}
