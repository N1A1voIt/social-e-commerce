package com.itu.socialcom.demo.authentication.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Getter
@Setter
@Table(name = "seller_v2")
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seller")
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_provider")
    private ProviderType provider;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "firebase_uid", nullable = false)
    private String firebaseUid;

    public enum ProviderType {
        GOOGLE,
        FACEBOOK,
        BASIC,
        X,
        GITHUB
    }
}
