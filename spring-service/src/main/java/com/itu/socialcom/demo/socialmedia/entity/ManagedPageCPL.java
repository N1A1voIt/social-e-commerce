package com.itu.socialcom.demo.socialmedia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity
@Getter
@Setter
@Table(name = "v_managed_accounts")
public class ManagedPageCPL {
    @Id
    @Column(name = "id_mp")
    private Long idMp;

    @Column(name = "d_status")
    private String status;

    @Column(name = "platform_identifier")
    private String platformIdentifier;

    @Column(name = "page_title")
    private String pageTitle;

    @Column(name = "associated_media")
    private String associatedMedia;

    @Column(name = "link_to_platform")
    private String linkToPlatform;

    @Column(name = "platform")
    private String platform;

    @Column(name = "email")
    private String email;

    @Column(name = "id_seller")
    private Long idSeller;

    @Column(name = "username")
    private String username;
}
