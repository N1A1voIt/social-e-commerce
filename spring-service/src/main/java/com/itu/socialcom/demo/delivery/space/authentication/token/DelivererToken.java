package com.itu.socialcom.demo.delivery.space.authentication.token;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliverer_token")
@Data
public class DelivererToken {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @Column(name = "id_dd")
    private Long idDeliverer;
    @Column(name = "token")
    private String token;
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @PrePersist
    public void prePersist() {
        if (this.expiryDate == null) {
            this.expiryDate = LocalDateTime.now().plusDays(7); // default 7 days expiry
        }
    }
}
