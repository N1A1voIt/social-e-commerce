package com.itu.socialcom.demo.socialmedia.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "mp_payment_number")
@Data
public class ManagedPagesNumber {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @Column(name = "id_spn")
    private Long idSpn;
    @Column(name = "id_mp")
    private Long idMp;
}
