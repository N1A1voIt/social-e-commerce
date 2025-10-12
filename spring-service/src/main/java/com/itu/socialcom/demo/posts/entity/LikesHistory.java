package com.itu.socialcom.demo.posts.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "likes_history")
public class LikesHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lh", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reactions")
    private Integer reactions;

    @Column(name = "id_child")
    private Integer idChild;

    @NotNull
    @Column(name = "id_pc", nullable = false)
    private String idPc;

    // Constructors
    public LikesHistory() {}

    public LikesHistory(LocalDateTime createdAt, Integer reactions, Integer idChild, String idPc) {
        this.createdAt = createdAt;
        this.reactions = reactions;
        this.idChild = idChild;
        this.idPc = idPc;
    }
}
