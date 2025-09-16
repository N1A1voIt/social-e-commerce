package com.itu.socialcom.demo.orders.deliveryapplicants;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "v_delivery_applicants")
@Data
@Access(AccessType.FIELD)
public class DeliveryApplicant {
    @Id
    @Column(name = "id_di")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "shipping_address")
    private String shippingAddress;
    @Column(name = "id_delivery")
    private Long idDelivery;
    @Column(name = "id_shp")
    private Long idShp;
    @Column(name = "d_status")
    private String dStatus;
    @Column(name = "amount")
    private Double amount;
    @Column(name = "distance")
    private Double distance;
    @Column(name = "id_dd")
    private Long idDd;
    @Column(name = "driver_name")
    private String driverName;
    @Column(name = "driver_phone")
    private String driverPhone;
    @Column(name = "id_mp")
    private Long idMp;
    @Column(name = "page_title")
    private String pageTitle;
    @Column(name = "id_seller")
    private Integer idSeller;
    @Column(name = "email")
    private String email;
    @Column(name = "username")
    private String username;
}
