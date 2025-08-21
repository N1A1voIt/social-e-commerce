package com.itu.socialcom.demo.orders;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "order_details_v2")
@Data
public class OrderChild {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "id_order_details")
    private Long idOrderDetails;
    @Column(name = "price")
    private Double price;
    @Column(name = "quantity")
    private Double quantity;
    @Column(name = "id_variant")
    private Long idVariant;
    @Column(name = "id_product")
    private Long idProduct;
    @Column(name = "id_order_m")
    private Long idOrderM;
    @Column(name = "media_url")
    private String mediaUrl;
    @Column(name = "product_name")
    private String productName;

}
