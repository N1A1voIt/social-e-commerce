package com.itu.socialcom.demo.orders;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Immutable()
@Table(name = "v_order_mother_cpl")
@Entity()
public class OrderMotherCpl {
    @Id
    @Column(name = "id_order_m")
    private Long idOrderM;
    @Column(name = "description")
    private String description;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "d_total")
    private Double dTotal;
    @Column(name = "d_customer_name")
    private String dCustomerName;
    @Column(name = "d_status")
    private Integer dStatus;
    @Column(name = "shipping_address")
    private String shippingAddress;
    @Column(name = "customer_number")
    private String customerNumber;
    @Column(name = "id_pc")
    private String idPc;
    @Column(name = "id_seller")
    private Integer idSeller;
    @Column(name = "id_managed_pages")
    private Integer idManagedPages;
    @Column(name = "id_sp")
    private Integer idSp;
    @Column(name = "page_title")
    private String pageTitle;
}
