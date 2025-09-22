package com.itu.socialcom.demo.orders.tempLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TempLinkRepository extends JpaRepository<TempLink, String> {
    Optional<TempLink> findByTempLink(String tempLink);
    Optional<TempLink> findById(String id);
}
