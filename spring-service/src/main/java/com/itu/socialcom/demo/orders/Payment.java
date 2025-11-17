package com.itu.socialcom.demo.orders;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_payment")
    private Long id;
    
    @Column(name = "id_sales")
    private Integer salesId;
    
    @Column(name = "amount")
    private Double amount;
    
    @Column(name = "id_pm")
    private Integer paymentMethodId;
    
    @Column(name = "d_payment_method")
    private String paymentMethodName;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
