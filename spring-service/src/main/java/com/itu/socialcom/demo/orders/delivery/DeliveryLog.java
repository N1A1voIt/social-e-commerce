package com.itu.socialcom.demo.orders.delivery;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name = "delivery_log")
@Entity
public class DeliveryLog {
    @Id
    @Column(name = "id_di")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "id_seller")
    private Integer idSeller;
    @Column(name = "message")
    private String message;
    @Column(name = "contact")
    private String contact;
    @Column(name = "id_dd")
    private Long idDd;
    @Column(name = "id_delivery")
    private Long idDelivery;
    @Column(name = "id_mp")
    private Long idMp;
}
