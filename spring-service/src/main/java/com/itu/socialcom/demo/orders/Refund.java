package com.itu.socialcom.demo.orders;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "refund")
@Data
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_refund")
    private Long id;
    @Column(name = "id_order")
    private Long orderId;
    @Column(name = "amount")
    private Double amount;
    @Column(name = "id_sale")
    private Integer saleId;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
