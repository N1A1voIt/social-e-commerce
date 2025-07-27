package com.itu.socialcom.demo.posts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

/**
 * Mapping for DB view
 */
@Getter
@Setter
@Entity
@Immutable
@Table(name = "v_refresh_token_holder")
public class VRefreshTokenHolder {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "id_mp")
    private Integer idMp;

    @Size(max = 50)
    @Column(name = "d_status", length = 50)
    private String dStatus;

    @Column(name = "platform_identifier", length = Integer.MAX_VALUE)
    private String platformIdentifier;

    @Column(name = "page_title", length = Integer.MAX_VALUE)
    private String pageTitle;

    @Column(name = "associated_media", length = Integer.MAX_VALUE)
    private String associatedMedia;

    @Column(name = "link_to_platform", length = Integer.MAX_VALUE)
    private String linkToPlatform;

    @Column(name = "id_sp")
    private Integer idSp;

    @Column(name = "id_seller")
    private Integer idSeller;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "email", length = Integer.MAX_VALUE)
    private String email;

    @Column(name = "token", length = Integer.MAX_VALUE)
    private String token;

}