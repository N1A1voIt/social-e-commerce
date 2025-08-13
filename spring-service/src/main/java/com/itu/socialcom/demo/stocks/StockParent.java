package com.itu.socialcom.demo.stocks;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Table(name = "stocks_v2")
@Entity
public class StockParent {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "id_mv")
    private Long id;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "id_order_m")
    private Long idOrderM;

    @Transient
    List<StockChild> stockChildren;

    @Transient
    boolean isEntry;

}
