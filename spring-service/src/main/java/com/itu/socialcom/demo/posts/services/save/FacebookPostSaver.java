package com.itu.socialcom.demo.posts.services.save;

import com.itu.socialcom.demo.authentication.user.Seller;
import com.itu.socialcom.demo.posts.dto.MediaDetails;
import com.itu.socialcom.demo.posts.dto.PostDetails;
import com.itu.socialcom.demo.posts.entity.PostChild;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class FacebookPostSaver implements SavePostService{
    public String uploadMediaUnpublished(MediaDetails mediaDetails) throws IOException {
        String pageId = mediaDetails.getPageId();
        String pageAccessToken = mediaDetails.getPageAccessToken();
        String imageUrl = mediaDetails.getImageUrl();
        String message = mediaDetails.getMessage();
        String url = String.format("https://graph.facebook.com/v20.0/%s/photos", pageId);

        HttpPost post = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        ContentType utf8Text = ContentType.create("text/plain", "UTF-8");
        builder.addTextBody("access_token", pageAccessToken, utf8Text);
        builder.addTextBody("message", message, utf8Text);
        builder.addTextBody("url", imageUrl, utf8Text);
        builder.addTextBody("published", "false", utf8Text);

        post.setEntity(builder.build());

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post)) {
            String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            System.out.println(json);
            JSONObject obj = new JSONObject(json);
            return obj.getString("id")+"__SEP__"+message+"__SEP__"+imageUrl;
        }
    }

    @Override
    public PostChild createPostWithMedia(PostDetails postDetails) throws IOException {
        String pageId = postDetails.getPageId();
        String pageAccessToken = postDetails.getPageAccessToken();
        List<String> mediaFbids = postDetails.getMediaIds();
        String message = postDetails.getMessage();
        String url = String.format("https://graph.facebook.com/v20.0/%s/feed", pageId);

        HttpPost post = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        ContentType utf8Text = ContentType.create("text/plain", "UTF-8");
        builder.addTextBody("access_token", pageAccessToken, utf8Text);
        builder.addTextBody("message", message, utf8Text);

        for (int i = 0; i < mediaFbids.size(); i++) {
            JSONObject media = new JSONObject();
            media.put("media_fbid", mediaFbids.get(i).split("__SEP__")[0]);
            builder.addTextBody(String.format("attached_media[%d]", i), media.toString(), utf8Text);
        }

        post.setEntity(builder.build());

        try (CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(post)) {

            String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(json);
            String postId = obj.getString("id");
            String postUrl = "https://www.facebook.com/" + postId;

            PostChild mother = new PostChild();
            mother.setPostUrl(postUrl);
            mother.setPlatformIdentifier(postId);
            mother.setDescription(message);
            mother.setType("main_post");
            mother.setIdSp(1L);
            mother.setIdPost(null);
            mother.setIdChild1(null);

            List<PostChild> children = new ArrayList<>();
            for (String mediaFbid : mediaFbids) {
                PostChild child = new PostChild();
                child.setPostUrl(postUrl);

                child.setMediaUrl(mediaFbid.split("__SEP__")[2]);
                child.setPlatformIdentifier(mediaFbid.split("__SEP__")[0]);
                child.setDescription(mediaFbid.split("__SEP__")[1]);
                child.setType("photo");
                child.setIdSp(1L);
                child.setIdPost(null);
                child.setIdChild1(null);
                children.add(child);
            }
            mother.setPostChilds(children);
            return mother;
        }
    }

    public PostChild schedulePostWithMedia(PostDetails postDetails, long scheduledUnixTime) throws IOException {
        String pageId = postDetails.getPageId();
        String pageAccessToken = postDetails.getPageAccessToken();
        List<String> mediaFbids = postDetails.getMediaIds();
        String message = postDetails.getMessage();
        String url = String.format("https://graph.facebook.com/v20.0/%s/feed", pageId);

        HttpPost post = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        ContentType utf8Text = ContentType.create("text/plain", "UTF-8");
        builder.addTextBody("access_token", pageAccessToken, utf8Text);
        builder.addTextBody("message", message, utf8Text);
        builder.addTextBody("published", "false", utf8Text);
        builder.addTextBody("scheduled_publish_time", String.valueOf(scheduledUnixTime), utf8Text);

        for (int i = 0; i < mediaFbids.size(); i++) {
            JSONObject media = new JSONObject();
            media.put("media_fbid", mediaFbids.get(i).split("__SEP__")[0]);
            builder.addTextBody(String.format("attached_media[%d]", i), media.toString(), utf8Text);
        }

        post.setEntity(builder.build());

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post)) {
            String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(json);
            String postId = obj.optString("id", null);

            PostChild mother = new PostChild();
            mother.setPostUrl(postId != null ? ("https://www.facebook.com/" + postId) : "null");
            mother.setPlatformIdentifier("facebook");
            mother.setDescription(message);
            mother.setType("scheduled_post");
            mother.setIdSp(1L);
            mother.setIdPost(null);
            mother.setIdChild1(null);

            List<PostChild> children = new ArrayList<>();
            for (String mediaFbid : mediaFbids) {
                PostChild child = new PostChild();
                child.setPostUrl(postId != null ? ("https://www.facebook.com/" + postId) : "null");
                child.setPlatformIdentifier(mediaFbid.split("__SEP__")[0]);
                child.setDescription(mediaFbid.split("__SEP__")[1]);
                child.setType("photo");
                child.setIdSp(1L);
                child.setIdPost(null);
                child.setIdChild1(null);
                children.add(child);
            }
            mother.setPostChilds(children);
            return mother;
        }
    }
}
