package com.itu.socialcom.demo.posts.services.save;

import com.itu.socialcom.demo.posts.dto.MediaDetails;
import com.itu.socialcom.demo.posts.dto.SavePostArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Service
public class GeneralPostSaver {
    @Autowired
    FacebookPostSaver facebookPostSaver;
    private void func(SavePostArgs savePostArgs) {
        for (int i = 0; i < savePostArgs.getPagesIds().size(); i++) {

        }
//        for (String imageUrl : imageUrls) {
//            mediaFbids.add(facebookPostSaver.uploadMediaUnpublished(pageId, imageUrl, accessToken));
//        }
//
//        facebookPostSaver.createPostWithMedia(pageId, mediaFbids, "Here are some awesome pictures!", accessToken);
    }
}
