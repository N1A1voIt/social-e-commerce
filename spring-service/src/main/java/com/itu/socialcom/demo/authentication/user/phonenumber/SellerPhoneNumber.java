package com.itu.socialcom.demo.authentication.user.phonenumber;

import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.moneytransactions.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sellers_phone_number_e")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerPhoneNumber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_spn")
    private Long id;

    @Column(name = "phone_number", nullable = false, length = 50)
    private String phoneNumber;

    @Column(name = "associated_name", nullable = false)
    private String associatedName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seller", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_pm", nullable = false)
    private PaymentMethod paymentMethod;
}
