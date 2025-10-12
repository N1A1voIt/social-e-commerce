package com.itu.socialcom.demo.posts.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "medias")
@NoArgsConstructor
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "media_url", nullable = false, length = Integer.MAX_VALUE)
    private String mediaUrl;

    @NotNull
    @Column(name = "id_child", nullable = false)
    private Integer idChild;

    public Media(String mainMediaUrl, String type) {
        this.setMediaUrl(mainMediaUrl);
    }

    @Override
    public String toString() {
        return "Media{" +
                "id=" + id +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", idChild=" + idChild +
                '}';
    }
}