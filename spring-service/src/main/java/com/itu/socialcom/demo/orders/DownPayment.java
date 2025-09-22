package com.itu.socialcom.demo.orders;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Table(name = "down_payment_parameter")
@Data
public class DownPayment {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "start_at")
    private LocalDateTime startAt;
    @Column(name = "end_at")
    private LocalDateTime endAt;
    @Column(name = "payment_in_percent")
    private Double paymentInPercent;
    @Column(name = "id_seller")
    private Long idSeller;
}
