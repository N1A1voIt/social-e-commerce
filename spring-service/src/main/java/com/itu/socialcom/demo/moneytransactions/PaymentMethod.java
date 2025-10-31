package com.itu.socialcom.demo.moneytransactions;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_method_v2")
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pm")
    private Long id;

    @Column(name = "payment_name")
    private String paymentName;
}

