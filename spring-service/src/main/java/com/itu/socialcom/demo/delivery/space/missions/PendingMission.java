package com.itu.socialcom.demo.delivery.space.missions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "v_pending_request")
@Immutable
@Data
public class PendingMission {
    @Id
    @Column(name = "id_di")  // Assuming this is the best candidate for @Id
    private Long idDi;

    @Column(name = "id_delivery")
    private Long idDelivery;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "d_status")
    private String dStatus;

    @Column(name = "id_order_m")
    private Long idOrderM;

    @Column(name = "id_dd")
    private Long idDd;

    @Column(name = "id_shp")
    private Long idShp;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "origin")
    private String origin;

    @Column(name = "place_name")
    private String placeName;

    @Column(name = "log_id_deliverer")
    private Long logIdDeliverer;
}
