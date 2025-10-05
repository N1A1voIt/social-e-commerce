package com.itu.socialcom.demo.posts.repository;

import com.itu.socialcom.demo.posts.entity.LikesHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikesHistoryRepository extends JpaRepository<LikesHistory, Integer> {
    List<LikesHistory> findByIdChild(Integer idChild);
    
    @Query("SELECT lh FROM LikesHistory lh WHERE lh.idChild = :idChild ORDER BY lh.createdAt ASC")
    List<LikesHistory> findByIdChildAndIdPc(@Param("idChild") Integer idChild);
}
