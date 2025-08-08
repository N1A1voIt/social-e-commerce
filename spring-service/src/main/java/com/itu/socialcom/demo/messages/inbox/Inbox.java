package com.itu.socialcom.demo.messages.inbox;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "inbox")
public class Inbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_im")
    private Integer id;

    @Column(name = "id_mp", nullable = false)
    private Integer idMp;

    // Getters and Setters
}

