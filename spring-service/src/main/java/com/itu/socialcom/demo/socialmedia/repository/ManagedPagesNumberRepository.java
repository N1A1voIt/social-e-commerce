package com.itu.socialcom.demo.socialmedia.repository;

import com.itu.socialcom.demo.socialmedia.entity.ManagedPagesNumber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManagedPagesNumberRepository extends JpaRepository<ManagedPagesNumber, Long> {
    ManagedPagesNumber findByIdMp(Long idMp);
    Optional<ManagedPagesNumber> findByIdMpAndIdPm(Long idMp, Long idPm);
    boolean existsByIdMpAndIdPm(Long idMp, Long idPm);
}
