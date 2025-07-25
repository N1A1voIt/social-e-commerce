package com.itu.socialcom.demo.products.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "category")
@Getter
@Setter
public class Category {
    @Id
    @Column(name = "id_category")
    Long idCategory;
    @Column(name = "val", nullable = false)
    String val;
    @Column(name = "desc_")
    String description;
}
