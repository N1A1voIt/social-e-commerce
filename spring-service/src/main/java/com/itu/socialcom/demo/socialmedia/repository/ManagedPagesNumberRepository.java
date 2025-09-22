package com.itu.socialcom.demo.socialmedia.repository;

import com.itu.socialcom.demo.socialmedia.entity.ManagedPagesNumber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagedPagesNumberRepository extends JpaRepository<ManagedPagesNumber, Long> {
    ManagedPagesNumber findByIdMp(Long idMp);
}
