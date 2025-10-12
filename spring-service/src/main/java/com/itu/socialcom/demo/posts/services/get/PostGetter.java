package com.itu.socialcom.demo.posts.services.get;

import com.itu.socialcom.demo.posts.dto.DisplayPost;
import com.itu.socialcom.demo.posts.entity.Media;
import com.itu.socialcom.demo.posts.entity.PostChildMedia;
import com.itu.socialcom.demo.posts.repository.MediaRepository;
import com.itu.socialcom.demo.posts.repository.PostChildMediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PostGetter {
    @Autowired
    PostChildMediaRepository postChildMediaRepository;
    @Autowired
    MediaRepository mediaRepository;

    public HashMap<Integer, List<String>> getMedia() {
        HashMap<Integer, List<String>> mediaMap = new HashMap<>();
        List<Media> media = mediaRepository.findAll();
        for (Media m : media) {
            System.out.println("media:"+m.toString());
            if (m.getIdChild() != null) {
                mediaMap.computeIfAbsent(m.getIdChild(), k -> new ArrayList<>()).add(m.getMediaUrl());
            }
        }
        return mediaMap;
    }

    public List<DisplayPost> mapToDisplayPosts(List<PostChildMedia> mediaList) {
        Map<Long, List<PostChildMedia>> groupedByPost = mediaList.stream()
                .collect(Collectors.groupingBy(PostChildMedia::getIdPost));

        List<DisplayPost> displayPosts = new ArrayList<>();

        HashMap<Integer, List<String>> mediaMap = getMedia();

        for (Map.Entry<Long, List<PostChildMedia>> entry : groupedByPost.entrySet()) {
            List<PostChildMedia> postMedias = entry.getValue();

            // Assume all rows have same platform & message info
            PostChildMedia base = postMedias.get(0);

            DisplayPost displayPost = new DisplayPost();
            displayPost.setId(base.getIdPost());
            displayPost.setPlatform(base.getSupportedPlatform());
            displayPost.setUsername(base.getPageTitle());
            displayPost.setMessage(base.getDescription());
            if (!"facebook".equalsIgnoreCase(base.getSupportedPlatform())) {
                // Map medias
                List<Media> mediaObjects = new ArrayList<>();
                mediaMap.get(base.getIdChild().intValue()).forEach(mediaUrl -> {;
                    Media media = new Media();
                    media.setMediaUrl(mediaUrl);
                    media.setIdChild(base.getIdChild().intValue());
                    mediaObjects.add(media);
                });
                displayPost.setMedias(mediaObjects);
            }

            // Add childPosts if platform is Facebook
            if ("facebook".equalsIgnoreCase(base.getSupportedPlatform())) {
                Map<Long, List<PostChildMedia>> childrenGrouped = postMedias.stream()
                        .filter(m -> m.getIdChild1() != null)
                        .collect(Collectors.groupingBy(PostChildMedia::getIdChild1));

                List<DisplayPost> childPosts = childrenGrouped.entrySet().stream()
                        .map(e -> {
                            List<PostChildMedia> children = e.getValue();
                            PostChildMedia baseChild = children.get(0);

                            DisplayPost child = new DisplayPost();
                            child.setId(baseChild.getIdChild1());
                            child.setPlatform(baseChild.getSupportedPlatform());
                            child.setUsername(baseChild.getPageTitle());
                            child.setMessage(baseChild.getDescription());
                            
                            List<Media> childMedia = children.stream()
                                    .filter(m -> m.getMainMediaUrl() != null)
                                    .map(m -> new Media(m.getMainMediaUrl(), m.getType()))
                                    .collect(Collectors.toList());

                            child.setMedias(childMedia);
                            return child;
                        }).collect(Collectors.toList());


                displayPost.setChildPosts(childPosts);
            }

            displayPosts.add(displayPost);
        }

        return displayPosts;
    }

}
