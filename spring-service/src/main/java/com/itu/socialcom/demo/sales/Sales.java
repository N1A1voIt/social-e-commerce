package com.itu.socialcom.demo.sales;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
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

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesDetails> details = new ArrayList<>();

    // Constructors
    public Sales() {}

    // Getters / Setters (only essential shown)
    public Integer getIdSale() { return idSale; }
    public void setIdSale(Integer idSale) { this.idSale = idSale; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getEffectuatedAt() { return effectuatedAt; }
    public void setEffectuatedAt(LocalDateTime effectuatedAt) { this.effectuatedAt = effectuatedAt; }

    public String getFromNumber() { return fromNumber; }
    public void setFromNumber(String fromNumber) { this.fromNumber = fromNumber; }

    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getIdSpn() { return idSpn; }
    public void setIdSpn(Integer idSpn) { this.idSpn = idSpn; }

    public Integer getIdOrderM() { return idOrderM; }
    public void setIdOrderM(Integer idOrderM) { this.idOrderM = idOrderM; }

    public String getIdPc() { return idPc; }
    public void setIdPc(String idPc) { this.idPc = idPc; }

    public List<SalesDetails> getDetails() { return details; }
    public void setDetails(List<SalesDetails> details) { this.details = details; }
}

