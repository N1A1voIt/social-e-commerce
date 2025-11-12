package com.itu.socialcom.demo.sales;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_payment")
    private Long idPayment;
    @Column(name = "id_sales")
    private Long idSales;
    @Column(name = "amount")
    private Double amount;
    @Column(name = "id_pm")
    private Long idPm;
    @Column(name = "d_payment_method")
    private String paymentMethod;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
