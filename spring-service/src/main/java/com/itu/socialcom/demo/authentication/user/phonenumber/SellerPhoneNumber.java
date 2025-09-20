package com.itu.socialcom.demo.authentication.user.phonenumber;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sellers_phone_number_e")
@Data
public class SellerPhoneNumber {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "id_spn")
    private Long id;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "id_seller")
    private Long idSeller;
    @Column(name = "associated_name")
    private String associatedName;
    @Column(name = "id_pm")
    private Long idPm;
}
