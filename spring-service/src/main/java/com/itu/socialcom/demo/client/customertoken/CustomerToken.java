package com.itu.socialcom.demo.client.customertoken;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_token")
@Getter
@Setter
public class CustomerToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Long idToken;
    @Column(name = "token")
    private String token;
    @Column(name = "id_customer", nullable = false)
    private Long idCustomer;
    @Column(nullable = false,name = "expired_at")
    private LocalDateTime expiryDate;
}
