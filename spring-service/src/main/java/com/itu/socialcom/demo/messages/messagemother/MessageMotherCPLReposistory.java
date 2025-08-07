package com.itu.socialcom.demo.messages.messagemother;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageMotherCPLReposistory extends JpaRepository<MessageMotherCPL,Long> {
    List<MessageMotherCPL> findByIdMp(Long idMp);
}
