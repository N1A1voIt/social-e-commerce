package com.itu.socialcom.demo.orders;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "order_mother")
public class OrderParent {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
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
    @Column(name = "id_cart")
    private Integer idCart;
    @Column (name = "id_customer")
    private Integer idCustomer;
    @Transient
    private Double downPPercent;
    @Transient
    private Double downP;
    @Transient
    private List<OrderChild> childs;


    @Override
    public String toString() {
        return "OrderParent{" +
                "idOrderM=" + idOrderM +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", dTotal=" + dTotal +
                ", dCustomerName='" + dCustomerName + '\'' +
                ", dStatus=" + dStatus +
                ", shippingAddress='" + shippingAddress + '\'' +
                ", customerNumber='" + customerNumber + '\'' +
                ", idPc='" + idPc + '\'' +
                ", idSeller=" + idSeller +
                ", idManagedPages=" + idManagedPages +
                ", downPPercent=" + downPPercent +
                ", downP=" + downP +
                '}';
    }
}
