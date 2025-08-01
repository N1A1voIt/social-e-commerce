package com.itu.socialcom.demo.posts.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MotherPostDisplay {
    Long idPost;
    boolean scheduled;
    String title;
    LocalDateTime creationDate;
}
