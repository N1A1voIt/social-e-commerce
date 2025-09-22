package com.itu.socialcom.demo.delivery.space.missions;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PendingMissionRepository extends JpaRepository<PendingMission,Long> {

    List<PendingMission> findByIdDd(Long idDd);

    List<PendingMission> findByLogIdDeliverer(Long logIdDeliverer);
}
