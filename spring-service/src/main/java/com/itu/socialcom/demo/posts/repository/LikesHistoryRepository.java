package com.itu.socialcom.demo.posts.repository;

import com.itu.socialcom.demo.posts.entity.LikesHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikesHistoryRepository extends JpaRepository<LikesHistory, Integer> {
    
    // Basic queries
    List<LikesHistory> findByIdChild(Integer idChild);
    
    @Query("SELECT lh FROM LikesHistory lh WHERE lh.idChild = :idChild ORDER BY lh.createdAt ASC")
    List<LikesHistory> findByIdChildAndIdPc(@Param("idChild") Integer idChild);
    
    // Statistics queries
    @Query("SELECT pc.idSp as platformId, COUNT(lh.id) as likesCount " +
           "FROM LikesHistory lh " +
           "JOIN PostChild pc ON lh.idChild = pc.id " +
           "WHERE pc.idPost = :postId " +
           "GROUP BY pc.idSp")
    List<Object[]> getPlatformDistribution(@Param("postId") Integer postId);
    
    @Query("SELECT DATE(lh.createdAt) as date, pc.idSp as platformId, " +
           "       COUNT(lh.id) as likesCount " +
           "FROM LikesHistory lh " +
           "JOIN PostChild pc ON lh.idChild = pc.id " +
           "WHERE pc.idPost = :postId " +
           "GROUP BY DATE(lh.createdAt), pc.idSp " +
           "ORDER BY DATE(lh.createdAt) ASC")
    List<Object[]> getLikesTimeSeries(@Param("postId") Integer postId);
}
