package com.itu.socialcom.demo.messages.inbox;

import com.itu.socialcom.demo.products.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InboxRepository extends JpaRepository<Inbox, Integer> {
    Optional<Inbox> findByIdMp(Integer idMp);
}

