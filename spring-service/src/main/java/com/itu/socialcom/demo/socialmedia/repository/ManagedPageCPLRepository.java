package com.itu.socialcom.demo.socialmedia.repository;

import com.itu.socialcom.demo.socialmedia.entity.ManagedPageCPL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManagedPageCPLRepository extends JpaRepository<ManagedPageCPL,Long> {
    List<ManagedPageCPL> findByIdSeller(Long idSeller);
    ManagedPageCPL findByIdMp(Long idMp);
    ManagedPageCPL findByPlatformIdentifierAndPlatform(String platformIdentifier, String platform);

    List<ManagedPageCPL> findByIdSellerAndPlatform(Long idSeller, String platform);
}
