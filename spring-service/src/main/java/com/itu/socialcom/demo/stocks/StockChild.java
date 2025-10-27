package com.itu.socialcom.demo.stocks;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stocks_child")
public class StockChild {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "id_st_ch")
    Long idStCh;
    @Column(name = "price")
    Double price;
    @Column(name = "action_at")
    LocalDateTime actionAt;
    @Column(name = "id_mv")
    Long idMv;
    @Column(name="input")
    Double input;
    @Column(name = "output")
    Double output;
    @Column(name = "d_product_number")
    Double dProductNumber;
    @Column(name = "d_variant_number")
    Double dVariantNumber;
    @Column(name = "id_product")
    Long idProduct;
    @Column(name = "id_variant")
    Long idVariant;
    @Column(name = "created_at")
    LocalDateTime createdAt;
    @Column(name = "product_name")
    String productName;
    @Column(name = "variant_name")
    String variantName;
}
