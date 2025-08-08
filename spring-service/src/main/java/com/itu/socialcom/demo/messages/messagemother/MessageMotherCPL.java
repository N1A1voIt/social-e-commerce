package com.itu.socialcom.demo.messages.messagemother;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Immutable;

@Entity
@Data
@Immutable
@Table(name = "v_message_box")
public class MessageMotherCPL {
    @Id
    @Column(name = "id_mm")
    private Long idMm;
    @Column(name = "id_pc")
    private String idPc;
    @Column(name = "id_mp")
    private Long idMp;
    @Column(name = "id_im")
    private Long idIm;
    @Column(name = "name")
    private String name;
    @Column(name = "link_to_profile")
    private String linkToProfile;
    @Column(name = "d_platform")
    private String platform;
    @Column(name = "identifier_on_platform")
    private String identifierOnPlatform;
    @Column(name = "media_url")
    private String mediaUrl;
    @Column(name = "id_sp")
    private Long supportedPlatform;
}
