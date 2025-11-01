package com.itu.socialcom.demo.socialmedia.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "v_mp_numbers")
@Immutable
@Data
public class VMpNumbers {

    @Id
    private Long id;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "associated_name")
    private String associatedName;

    @Column(name = "id_pm")
    private Long idPm;

    @Column(name = "id_mp")
    private Long idMp;

    @Column(name = "payment_name")
    private String paymentName;

    @Column(name = "id_spn")
    private Long idSpn;
}