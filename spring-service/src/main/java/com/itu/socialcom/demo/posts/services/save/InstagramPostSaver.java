package com.itu.socialcom.demo.posts.services.save;

import com.itu.socialcom.demo.posts.dto.MediaDetails;
import com.itu.socialcom.demo.posts.dto.PostDetails;
import com.itu.socialcom.demo.posts.entity.Media;
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
import java.util.stream.Collectors;

@Service
public class InstagramPostSaver implements SavePostService {

    @Override
    public String uploadMediaUnpublished(MediaDetails mediaDetails) throws IOException {
        String instagramUserId = mediaDetails.getPageId();
        String accessToken = mediaDetails.getPageAccessToken();
        String imageUrl = mediaDetails.getImageUrl();
        String caption = mediaDetails.getMessage();
        System.out.println("URL: "+imageUrl);
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
            return obj.getString("id") + "__SEP__" + (caption.isEmpty()? "banga" : caption) +"__SEP__"+imageUrl;
        }
    }

    @Override
    public PostChild createPostWithMedia(PostDetails postDetails) throws IOException {
        String instagramUserId = postDetails.getPageId();
        String accessToken = postDetails.getPageAccessToken();
        List<String> mediaIds = postDetails.getMediaIds();

        String caption = postDetails.getMessage();

        if (mediaIds.isEmpty()) {
            throw new IllegalArgumentException("No media IDs provided");
        }

        String creationId;

        if (mediaIds.size() == 1) {
            // Single media post
            creationId = mediaIds.get(0).split("__SEP__")[0];
        } else {
            // Carousel post
            String childrenIds = mediaIds.stream()
                    .map(id -> id.split("__SEP__")[0])
                    .collect(Collectors.joining(","));

            String carouselUrl = String.format("https://graph.facebook.com/v23.0/%s/media", instagramUserId);
            HttpPost carouselPost = new HttpPost(carouselUrl);
            MultipartEntityBuilder carouselBuilder = MultipartEntityBuilder.create();
            carouselBuilder.addTextBody("access_token", accessToken);
            carouselBuilder.addTextBody("media_type", "CAROUSEL");
            carouselBuilder.addTextBody("children", childrenIds);
            carouselBuilder.addTextBody("caption", caption);

            carouselPost.setEntity(carouselBuilder.build());

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(carouselPost)) {

                String json = EntityUtils.toString(response.getEntity());
                System.out.println("Instagram carousel container response: " + json);
                JSONObject obj = new JSONObject(json);
                creationId = obj.getString("id");
            }
        }

        // Publish the post
        String publishUrl = String.format("https://graph.facebook.com/v23.0/%s/media_publish", instagramUserId);
        HttpPost publishPost = new HttpPost(publishUrl);
        MultipartEntityBuilder publishBuilder = MultipartEntityBuilder.create();
        publishBuilder.addTextBody("access_token", accessToken);
        publishBuilder.addTextBody("creation_id", creationId);

        publishPost.setEntity(publishBuilder.build());

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(publishPost)) {

            String json = EntityUtils.toString(response.getEntity());
            System.out.println("Instagram publish response: " + json);
            JSONObject obj = new JSONObject(json);
            String postId = obj.getString("id");
            String postUrl = "https://www.instagram.com/p/" + postId;

            // Build PostChild object
            PostChild mother = new PostChild();
            mother.setPostUrl(postUrl);
            mother.setPlatformIdentifier("instagram");
            mother.setDescription(caption);
            mother.setType("main_post");
            mother.setIdSp(2L); // Instagram platform ID
            mother.setIdPost(null);
            mother.setIdChild1(null);

            // Children for carousel or single media
            List<Media> mediaList = new ArrayList<>();
            List<PostChild> children = new ArrayList<>();
            for (String mediaId : mediaIds) {
                PostChild child = new PostChild();
                child.setPostUrl(postUrl);
                child.setMediaUrl("https://www.instagram.com/p/" + mediaId.split("__SEP__")[0]);
                child.setPlatformIdentifier(mediaId.split("__SEP__")[0]);
//                child.setDescription(caption);
                child.setType(mediaIds.size() == 1 ? "photo" : "carousel_photo");
                child.setIdSp(2L);
                child.setIdPost(null);
                child.setIdChild1(null);
                children.add(child);
                Media media = new Media();
                media.setMediaUrl(mediaId.split("__SEP__")[2]);
                mediaList.add(media);
            }
            mother.setMediaList(mediaList);
            mother.setPostChilds(children);
            return mother;
        }
    }


    public PostChild schedulePostWithMedia(PostDetails postDetails, long scheduledUnixTime) throws IOException {
        String instagramUserId = postDetails.getPageId();
        String accessToken = postDetails.getPageAccessToken();
        List<String> mediaIds = postDetails.getMediaIds();
        String caption = postDetails.getMessage();

        if (mediaIds.isEmpty()) {
            throw new IllegalArgumentException("No media IDs provided");
        }

        String creationId;
        if (mediaIds.size() == 1) {
            creationId = mediaIds.get(0).split("__SEP__")[0];
        } else {
            String childrenIds = mediaIds.stream()
                    .map(id -> id.split("__SEP__")[0])
                    .collect(Collectors.joining(","));

            String carouselUrl = String.format("https://graph.facebook.com/v23.0/%s/media", instagramUserId);
            HttpPost carouselPost = new HttpPost(carouselUrl);
            MultipartEntityBuilder carouselBuilder = MultipartEntityBuilder.create();
            carouselBuilder.addTextBody("access_token", accessToken);
            carouselBuilder.addTextBody("media_type", "CAROUSEL");
            carouselBuilder.addTextBody("children", childrenIds);
            carouselBuilder.addTextBody("caption", caption);
            carouselPost.setEntity(carouselBuilder.build());

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(carouselPost)) {
                String json = EntityUtils.toString(response.getEntity());
                System.out.println("Instagram carousel container response (scheduled): " + json);
                JSONObject obj = new JSONObject(json);
                creationId = obj.getString("id");
            }
        }

        String publishUrl = String.format("https://graph.facebook.com/v23.0/%s/media_publish", instagramUserId);
        HttpPost publishPost = new HttpPost(publishUrl);
        MultipartEntityBuilder publishBuilder = MultipartEntityBuilder.create();
        publishBuilder.addTextBody("access_token", accessToken);
        publishBuilder.addTextBody("creation_id", creationId);
        publishBuilder.addTextBody("publish_at", String.valueOf(scheduledUnixTime));
        publishPost.setEntity(publishBuilder.build());

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(publishPost)) {
            String json = EntityUtils.toString(response.getEntity());
            System.out.println("Instagram schedule publish response: " + json);
            JSONObject obj = new JSONObject(json);
            String mediaId = obj.optString("id", null);
            String postUrl = mediaId != null ? ("https://www.instagram.com/p/" + mediaId) : "not uploaded";

            System.out.println("Post Urlleee:"+postUrl);


            PostChild mother = new PostChild();
            mother.setPostUrl(postUrl);
            mother.setPlatformIdentifier("instagram");
            mother.setDescription(caption);
            mother.setType("scheduled_post");
            mother.setIdSp(2L);
            mother.setIdPost(null);
            mother.setIdChild1(null);

            List<PostChild> children = new ArrayList<>();
            for (String mediaIdWithCaption : mediaIds) {
                PostChild child = new PostChild();
                child.setPostUrl(postUrl);
                child.setPlatformIdentifier(mediaIdWithCaption.split("__SEP__")[0]);
                child.setDescription(mediaIdWithCaption.split("__SEP__")[1]);
                child.setType(mediaIds.size() == 1 ? "photo" : "carousel_photo");
                child.setIdSp(2L);
                child.setIdPost(null);
                child.setIdChild1(null);
                children.add(child);
            }
            mother.setPostChilds(children);
            return mother;
        }
    }
}
