package com.itu.socialcom.demo.posts.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "medias")
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "media_url", nullable = false, length = Integer.MAX_VALUE)
    private String mediaUrl;

    @NotNull
    @Column(name = "id_child", nullable = false)
    private Integer idChild;

}