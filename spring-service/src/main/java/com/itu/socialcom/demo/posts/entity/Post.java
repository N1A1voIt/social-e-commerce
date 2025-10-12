package com.itu.socialcom.demo.posts.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_post", nullable = false)
    private Integer id;

    @Size(max = 50)
    @Column(name = "type", length = 50)
    private String type;

    @NotNull
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @NotNull
    @Column(name = "id_seller", nullable = false)
    private Long idSeller;

    @Transient
    List<PostChild> postChildren;
    
    @Transient
    Boolean isExisting;
}