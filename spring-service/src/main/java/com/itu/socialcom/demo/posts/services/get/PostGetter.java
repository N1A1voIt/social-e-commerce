package com.itu.socialcom.demo.posts.services.get;

import com.itu.socialcom.demo.posts.dto.DisplayPost;
import com.itu.socialcom.demo.posts.entity.Media;
import com.itu.socialcom.demo.posts.entity.PostChildMedia;
import com.itu.socialcom.demo.posts.repository.PostChildMediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PostGetter {
    @Autowired
    PostChildMediaRepository postChildMediaRepository;
    public List<DisplayPost> mapToDisplayPosts(List<PostChildMedia> mediaList) {
        Map<Long, List<PostChildMedia>> groupedByPost = mediaList.stream()
                .collect(Collectors.groupingBy(PostChildMedia::getIdPost));

        List<DisplayPost> displayPosts = new ArrayList<>();

        for (Map.Entry<Long, List<PostChildMedia>> entry : groupedByPost.entrySet()) {
            List<PostChildMedia> postMedias = entry.getValue();

            // Assume all rows have same platform & message info
            PostChildMedia base = postMedias.get(0);

            DisplayPost displayPost = new DisplayPost();
            displayPost.setId(base.getIdPost());
            displayPost.setPlatform(base.getSupportedPlatform());
            displayPost.setUsername(base.getPageTitle());
            displayPost.setMessage(base.getDescription());

            // Map medias
            List<Media> mediaObjects = postMedias.stream()
                    .map(m -> new Media(m.getMainMediaUrl(), m.getType()))
                    .collect(Collectors.toList());
            displayPost.setMedias(mediaObjects);

            // Add childPosts if platform is Facebook
            if ("facebook".equalsIgnoreCase(base.getSupportedPlatform())) {
                List<DisplayPost> childPosts = postMedias.stream()
                        .filter(m -> m.getIdChild1() != null)
                        .map(m -> {
                            DisplayPost child = new DisplayPost();
                            child.setId(m.getIdChild1());
                            child.setPlatform(m.getSupportedPlatform());
                            child.setUsername(m.getPageTitle());
                            child.setMessage(m.getDescription());

                            List<Media> childMedia = new ArrayList<>();
                            if (m.getMainMediaUrl() != null) {
                                childMedia.add(new Media(m.getMainMediaUrl(), m.getType()));
                            }
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
