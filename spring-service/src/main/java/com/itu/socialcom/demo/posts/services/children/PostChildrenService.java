package com.itu.socialcom.demo.posts.services.children;

import com.itu.socialcom.demo.posts.dto.PostChildDisplay;
import com.itu.socialcom.demo.posts.entity.PostChild;
import com.itu.socialcom.demo.posts.entity.Media;
import com.itu.socialcom.demo.posts.repository.PostChildRepository;
import com.itu.socialcom.demo.posts.repository.MediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PostChildrenService {

    @Autowired
    private PostChildRepository postChildRepository;
    
    @Autowired
    private MediaRepository mediaRepository;

    public List<PostChildDisplay> getPostChildren(Integer postId) {
        // Get all post children for the given post
        List<PostChild> postChildren = postChildRepository.findByIdPost(postId);
        
        // Get all media for all post children
        List<Integer> childIds = postChildren.stream()
                .map(PostChild::getId)
                .collect(Collectors.toList());
        
        Map<Integer, List<Media>> mediaMap = getMediaMapForChildren(childIds);
        
        // Build hierarchical structure
        return buildPostChildHierarchy(postChildren, mediaMap);
    }

    private Map<Integer, List<Media>> getMediaMapForChildren(List<Integer> childIds) {
        return mediaRepository.findAll().stream()
                .filter(media -> childIds.contains(media.getIdChild()))
                .collect(Collectors.groupingBy(Media::getIdChild));
    }

    private List<PostChildDisplay> buildPostChildHierarchy(List<PostChild> postChildren, Map<Integer, List<Media>> mediaMap) {
        List<PostChildDisplay> result = new ArrayList<>();
        
        // Group children by whether they have a parent (id_child_1 is null means it's a main post)
        Map<Boolean, List<PostChild>> groupedChildren = postChildren.stream()
                .collect(Collectors.partitioningBy(child -> child.getIdChild1() == null));
        
        List<PostChild> mainPosts = groupedChildren.get(true); // Posts without parent (main posts)
        List<PostChild> attachmentPosts = groupedChildren.get(false); // Posts with parent (attachments)
        
        // Group attachments by their parent
        Map<Integer, List<PostChild>> attachmentsByParent = attachmentPosts.stream()
                .collect(Collectors.groupingBy(PostChild::getIdChild1));
        
        for (PostChild mainPost : mainPosts) {
            PostChildDisplay display = convertToDisplay(mainPost, mediaMap.get(mainPost.getId()));
            
            // Add attachments if they exist
            List<PostChild> attachments = attachmentsByParent.get(mainPost.getId());
            if (attachments != null && !attachments.isEmpty()) {
                List<PostChildDisplay> attachmentDisplays = attachments.stream()
                        .map(attachment -> convertToDisplay(attachment, mediaMap.get(attachment.getId())))
                        .collect(Collectors.toList());
                display.setAttachments(attachmentDisplays);
            }
            
            result.add(display);
        }
        
        return result;
    }

    private PostChildDisplay convertToDisplay(PostChild postChild, List<Media> mediaList) {
        PostChildDisplay display = new PostChildDisplay(
            postChild.getId(),
            postChild.getPostUrl(),
            postChild.getMediaUrl(),
            postChild.getDescription(),
            postChild.getPlatformIdentifier(),
            postChild.getType(),
            postChild.getIdSp(),
            postChild.getIdChild1(),
            postChild.getIdPost(),
            mediaList != null ? mediaList : new ArrayList<>()
        );
        return display;
    }
}
