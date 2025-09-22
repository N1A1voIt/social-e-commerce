package com.itu.socialcom.demo.posts.services.save;

import com.itu.socialcom.demo.posts.dto.MediaDetails;
import com.itu.socialcom.demo.posts.dto.PostDetails;
import com.itu.socialcom.demo.posts.entity.PostChild;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class InstagramPostSaver implements SavePostService {

    @Override
    public String uploadMediaUnpublished(MediaDetails mediaDetails) throws IOException {
        String instagramUserId = mediaDetails.getPageId();
        String accessToken = mediaDetails.getPageAccessToken();
        String imageUrl = mediaDetails.getImageUrl();
        String caption = mediaDetails.getMessage();

        // Instagram requires a container first for media uploads
        String containerUrl = String.format("https://graph.facebook.com/v23.0/%s/media", instagramUserId);

        HttpPost containerPost = new HttpPost(containerUrl);
        MultipartEntityBuilder containerBuilder = MultipartEntityBuilder.create();
        containerBuilder.addTextBody("access_token", accessToken);
        containerBuilder.addTextBody("image_url", imageUrl);
        containerBuilder.addTextBody("caption", caption);

        containerPost.setEntity(containerBuilder.build());

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(containerPost)) {
            String json = EntityUtils.toString(response.getEntity());
            System.out.println("Instagram container response: " + json);
            JSONObject obj = new JSONObject(json);
            return obj.getString("id") + "__SEP__" + caption;
        }
    }

    @Override
    public PostChild createPostWithMedia(PostDetails postDetails) throws IOException {
        String instagramUserId = postDetails.getPageId();
        String accessToken = postDetails.getPageAccessToken();
        List<String> mediaIds = postDetails.getMediaIds();
        String caption = postDetails.getMessage();

        // For Instagram, we need to publish the container
        String publishUrl = String.format("https://graph.facebook.com/v23.0/%s/media_publish", instagramUserId);

        // Instagram only allows publishing one media container at a time
        // We'll use the first media ID for this example
        String mediaContainerId = mediaIds.get(0).split("__SEP__")[0];

        HttpPost publishPost = new HttpPost(publishUrl);
        MultipartEntityBuilder publishBuilder = MultipartEntityBuilder.create();
        publishBuilder.addTextBody("access_token", accessToken);
        publishBuilder.addTextBody("creation_id", mediaContainerId);

        publishPost.setEntity(publishBuilder.build());

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(publishPost)) {

            String json = EntityUtils.toString(response.getEntity());
            System.out.println("Instagram publish response: " + json);
            JSONObject obj = new JSONObject(json);
            String postId = obj.getString("id");
            String postUrl = "https://www.instagram.com/p/" + postId;

            PostChild mother = new PostChild();
            mother.setPostUrl(postUrl);
            mother.setPlatformIdentifier("instagram");
            mother.setDescription(caption);
            mother.setType("main_post");
            mother.setIdSp(2L); // Instagram platform ID
            mother.setIdPost(null);
            mother.setIdChild1(null);

            List<PostChild> children = new ArrayList<>();
            for (String mediaId : mediaIds) {
                PostChild child = new PostChild();
                child.setPostUrl(postUrl);
                child.setMediaUrl("https://www.instagram.com/p/" + mediaId.split("__SEP__")[0]);
                child.setPlatformIdentifier(mediaId.split("__SEP__")[0]);
                child.setDescription(mediaId.split("__SEP__")[1]);
                child.setType("photo");
                child.setIdSp(2L);
                child.setIdPost(null);
                child.setIdChild1(null);
                children.add(child);
            }

            mother.setPostChilds(children);
            return mother;
        }
    }

    public PostChild schedulePostWithMedia(PostDetails postDetails, long scheduledUnixTime) throws IOException {


        String caption = postDetails.getMessage();

        PostChild mother = new PostChild();
        mother.setPlatformIdentifier("instagram");
        mother.setDescription(caption);
        mother.setType("scheduled_post");
        mother.setIdSp(2L); // Instagram platform ID
        mother.setIdPost(null);
        mother.setIdChild1(null);

        List<PostChild> children = new ArrayList<>();
        for (String mediaId : postDetails.getMediaIds()) {
            PostChild child = new PostChild();
            child.setPlatformIdentifier(mediaId.split("__SEP__")[0]);
            child.setDescription(mediaId.split("__SEP__")[1]);
            child.setType("photo");
            child.setIdSp(2L); // Instagram platform ID
            child.setIdPost(null);
            child.setIdChild1(null);
            children.add(child);
        }

        mother.setPostChilds(children);
        return mother;
    }
}
