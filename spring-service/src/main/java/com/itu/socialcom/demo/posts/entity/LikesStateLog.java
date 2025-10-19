package com.itu.socialcom.demo.posts.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes_state_log")
@Getter
@Setter
public class LikesStateLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 250)
    private String username;

    @Column(name = "id_user_platform", nullable = false, length = 250)
    private String idUserPlatform;

    @Column(name = "id_sp", nullable = false)
    private Integer idSp;

    @Column(length = 250)
    private String reaction;

    @Column(name = "id_child", nullable = false)
    private Integer idChild;

    @Column(name = "id_mp", nullable = false)
    private Integer idMp;

    @Column(name = "happened_at", nullable = false)
    private LocalDateTime happenedAt;

    @Override
    public String toString() {
        return "LikesStateLog{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", username='" + username + '\'' +
                ", idUserPlatform='" + idUserPlatform + '\'' +
                ", idSp=" + idSp +
                ", reaction='" + reaction + '\'' +
                ", idChild=" + idChild +
                ", idMp=" + idMp +
                ", happenedAt=" + happenedAt +
                '}';
    }
}


