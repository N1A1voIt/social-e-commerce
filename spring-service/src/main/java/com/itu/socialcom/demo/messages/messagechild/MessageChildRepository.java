package com.itu.socialcom.demo.messages.messagechild;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageChildRepository extends JpaRepository<MessageChild, Integer> {
}
