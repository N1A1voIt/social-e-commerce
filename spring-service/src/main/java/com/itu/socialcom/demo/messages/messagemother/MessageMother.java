package com.itu.socialcom.demo.messages.messagemother;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "message_mother")
public class MessageMother {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mm")
    private Integer id;

    @Column(name = "id_pc", nullable = false)
    private String idPc;

    @Column(name = "id_mp", nullable = false)
    private Integer idMp;

    @Column(name = "id_im", nullable = false)
    private Integer idIm;

    // Getters and Setters
}
