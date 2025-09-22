package com.itu.socialcom.demo.moneytransactions;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "mvola_tokens")
@Data
public class MvolaTokens {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Long id;
    @Column(name = "token")
    private String token;
    @Column(name = "start_date")
    private LocalDateTime startDate;
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;
    public boolean isExpired() {
        return expirationDate.isBefore(LocalDateTime.now());
    }
}
