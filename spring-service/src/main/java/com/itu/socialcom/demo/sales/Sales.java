package com.itu.socialcom.demo.sales;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
@Data
public class Sales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sale")
    private Integer idSale;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "effectued_at", nullable = false)
    private LocalDateTime effectuatedAt;

    @Column(name = "from_number", nullable = false, columnDefinition = "TEXT")
    private String fromNumber;

    @Column(name = "from_name", nullable = false, length = 50)
    private String fromName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "id_spn")
    private Integer idSpn;

    @Column(name = "id_order_m", nullable = false)
    private Integer idOrderM;

    @Column(name = "id_pc", nullable = false)
    private String idPc;

    @Column(name = "id_seller")
    private Integer idSeller;

    @Column(name = "paid_amount")
    private Double paidAmount;

    @Column(name = "status")
    private Integer status;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesDetails> details = new ArrayList<>();

    // Constructors
    public Sales() {}


}

