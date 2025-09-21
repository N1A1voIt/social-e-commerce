package com.itu.socialcom.demo.delivery.deliverydriver;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "delivery_driver_v2")
public class DeliveryDriver {
    @Id
    @Column(name = "id_dd")
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @Column(name = "email")
    private String email;
    @Column(name = "name")
    private String name;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "id_tt")
    private Integer idTt;
    @Column(name = "id_seller")
    private Integer idSeller;
    @Column(name = "min_range")
    private Double minRange;
    @Column(name = "max_range")
    private Double maxRange;
    @Column(name = "firebase_uid")
    private String firebaseUid;
}
