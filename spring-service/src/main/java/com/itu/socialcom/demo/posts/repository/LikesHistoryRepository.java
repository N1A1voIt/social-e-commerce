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
    @Query("""
            SELECT pc.idSp AS platformId, lh.reactions AS likesCount, lh.createdAt
            FROM LikesHistory lh
            JOIN PostChild pc ON lh.idChild = pc.id
            WHERE pc.idPost = :postId 
              AND pc.type = 'main_post'
              AND lh.createdAt = (
                  SELECT MAX(lh2.createdAt)
                  FROM LikesHistory lh2
                  JOIN PostChild pc2 ON lh2.idChild = pc2.id
                  WHERE pc2.idSp = pc.idSp 
                    AND pc2.idPost = :postId 
                    AND pc2.type = 'main_post'
              )
        """)
    List<Object[]> getPlatformDistribution(@Param("postId") Integer postId);

    @Query("""
        SELECT DATE(lh.createdAt) AS date, 
               pc.idSp AS platformId, 
               lh.reactions AS likesCount
        FROM LikesHistory lh
        JOIN PostChild pc ON lh.idChild = pc.id
        WHERE pc.idPost = :postId
          AND pc.type = 'main_post'
          AND lh.createdAt = (
              SELECT MAX(lh2.createdAt)
              FROM LikesHistory lh2
              JOIN PostChild pc2 ON lh2.idChild = pc2.id
              WHERE pc2.idSp = pc.idSp
                AND DATE(lh2.createdAt) = DATE(lh.createdAt)
                AND pc2.idPost = :postId
                AND pc2.type = 'main_post'
          )
        ORDER BY DATE(lh.createdAt) ASC
    """)
    List<Object[]> getLikesTimeSeries(@Param("postId") Integer postId);
}
