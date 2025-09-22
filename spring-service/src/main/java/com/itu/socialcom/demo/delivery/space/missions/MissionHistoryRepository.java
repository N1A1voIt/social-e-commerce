package com.itu.socialcom.demo.delivery.space.missions;

import com.itu.socialcom.demo.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionHistoryRepository extends JpaRepository<MissionHistory,Long> {
    List<MissionHistory> findByIdDd(Long idDd);

    List<MissionHistory> findByIdDdAndLogIdDeliverer(Long idDd, Long logIdDeliverer);
}
