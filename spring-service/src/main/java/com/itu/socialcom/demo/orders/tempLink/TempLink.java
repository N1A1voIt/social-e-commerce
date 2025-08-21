package com.itu.socialcom.demo.orders.tempLink;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "temp_payment_link")
public class TempLink {
    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String tempLink;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(name = "id_order_m",nullable = false)
    private Integer idOrderM;

    @Column(nullable = false)
    private Integer idSeller;

    @Column(name = "amount")
    private Double amount;

    @PrePersist
    public void prePersist() {
        if (this.expiredAt == null) {
            this.expiredAt = LocalDateTime.now().plusHours(1); // default 1h expiry
        }
    }
}

