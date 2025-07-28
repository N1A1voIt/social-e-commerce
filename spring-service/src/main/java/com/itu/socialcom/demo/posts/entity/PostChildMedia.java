package com.itu.socialcom.demo.posts.entity;

import com.google.errorprone.annotations.Immutable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "v_post_child_media")
@Immutable
public class PostChildMedia {

    @Id
    private Long id;

    @Column(name = "id_child")
    private Long idChild;

    @Column(name = "main_media_url")
    private String mainMediaUrl;

    private String description;

    @Column(name = "platform_identifier")
    private String platformIdentifier;

    private String type;

    @Column(name = "id_sp")
    private Long supportedPlatformId;

    @Column(name = "supported_platform")
    private String supportedPlatform;

    @Column(name = "id_seller")
    private Long idSeller;

    @Column(name = "id_child_1")
    private Long idChild1;

    @Column(name = "id_post")
    private Long idPost;

    @Column(name = "additional_media")
    private String additionalMedia;

    @Column(name = "page_title")
    private String pageTitle;

    @Column(name = "associated_media")
    private String profilePicture;
}
