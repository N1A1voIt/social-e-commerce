package com.itu.socialcom.demo.authentication.token;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tokens_v2")
public class TokenV2 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Long id;

    @Column(nullable = false, unique = true, length = 2000,name = "token")
    private String token;

    @Column(name = "id_seller", nullable = false)
    private Long userId;

    @Column(nullable = false,name = "expired_at")
    private LocalDateTime expiryDate;


}
