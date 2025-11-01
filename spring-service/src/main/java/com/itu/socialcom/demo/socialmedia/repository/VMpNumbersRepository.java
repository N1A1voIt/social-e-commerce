package com.itu.socialcom.demo.socialmedia.repository;

import com.itu.socialcom.demo.socialmedia.entity.VMpNumbers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VMpNumbersRepository extends JpaRepository<VMpNumbers, Long> {
    List<VMpNumbers> findByIdMp(Long idMp);
}
