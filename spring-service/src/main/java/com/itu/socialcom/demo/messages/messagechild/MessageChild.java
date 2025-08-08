package com.itu.socialcom.demo.messages.messagechild;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "message_child")
public class MessageChild {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mc")
    private Integer id;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "from_platform", nullable = false)
    private Boolean fromPlatform;

    @Column(name = "id_mm", nullable = false)
    private Integer idMm;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
}

