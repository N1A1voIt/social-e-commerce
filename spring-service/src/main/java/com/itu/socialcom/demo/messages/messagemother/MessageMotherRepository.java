package com.itu.socialcom.demo.messages.messagemother;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageMotherRepository extends JpaRepository<MessageMother, Integer> {
    Optional<MessageMother> findByIdPcAndIdIm(String idPc, Integer idIm);
    List<MessageMother> findByIdMp(Integer idMp);

    Integer id(Integer id);

    MessageMother findByIdPcAndIdMp(String idPc, Integer idMp);
}
