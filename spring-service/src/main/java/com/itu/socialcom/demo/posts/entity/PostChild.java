package com.itu.socialcom.demo.posts.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "post_childs")
public class PostChild {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_child", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "post_url", nullable = false, length = Integer.MAX_VALUE)
    private String postUrl;

    @Column(name = "media_url", length = Integer.MAX_VALUE)
    private String mediaUrl;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @NotNull
    @Column(name = "platform_identifier", nullable = false, length = Integer.MAX_VALUE)
    private String platformIdentifier;

    @Column(name = "type", length = Integer.MAX_VALUE)
    private String type;

    @NotNull
    @Column(name = "id_sp", nullable = false)
    private Long idSp;

    @Column(name = "id_child_1")
    private Integer idChild1;

    @NotNull
    @Column(name = "id_post", nullable = false)
    private Integer idPost;

    @Column(name = "id_mp")
    private Integer idMp;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Transient
    List<Media> mediaList;

    @Transient
    List<PostChild> postChilds;

}