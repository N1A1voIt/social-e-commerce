package com.itu.socialcom.demo.posts.dto;

import com.itu.socialcom.demo.posts.entity.Media;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostChildDisplay {
    private Integer id;
    private String postUrl;
    private String mediaUrl;
    private String description;
    private String platformIdentifier;
    private String type;
    private Long idSp;
    private Integer idChild1;
    private Integer idPost;
    private List<Media> mediaList;
    private List<PostChildDisplay> attachments; // For Facebook posts with attachments
    
    public PostChildDisplay() {}
    
    public PostChildDisplay(Integer id, String postUrl, String mediaUrl, String description, 
                           String platformIdentifier, String type, Long idSp, Integer idChild1, 
                           Integer idPost, List<Media> mediaList) {
        this.id = id;
        this.postUrl = postUrl;
        this.mediaUrl = mediaUrl;
        this.description = description;
        this.platformIdentifier = platformIdentifier;
        this.type = type;
        this.idSp = idSp;
        this.idChild1 = idChild1;
        this.idPost = idPost;
        this.mediaList = mediaList;
    }
}

