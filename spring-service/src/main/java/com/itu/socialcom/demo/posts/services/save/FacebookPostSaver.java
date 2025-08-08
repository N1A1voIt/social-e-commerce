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
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
        builder.addTextBody("access_token", pageAccessToken);
        builder.addTextBody("message", message);
        builder.addTextBody("url", imageUrl);
        builder.addTextBody("published", "false");

        post.setEntity(builder.build());

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post)) {
            String json = EntityUtils.toString(response.getEntity());
            System.out.println(json);
            JSONObject obj = new JSONObject(json);
            return obj.getString("id")+"__SEP__"+message;
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
        builder.addTextBody("access_token", pageAccessToken);
        builder.addTextBody("message", message);

        for (int i = 0; i < mediaFbids.size(); i++) {
            JSONObject media = new JSONObject();
            media.put("media_fbid", mediaFbids.get(i).split("__SEP__")[0]);
            builder.addTextBody(String.format("attached_media[%d]", i), media.toString());
        }

        post.setEntity(builder.build());

        try (CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(post)) {

            String json = EntityUtils.toString(response.getEntity());
            JSONObject obj = new JSONObject(json);
            String postId = obj.getString("id");
            String postUrl = "https://www.facebook.com/" + postId;

            PostChild mother = new PostChild();
            mother.setPostUrl(postUrl);
            mother.setPlatformIdentifier("facebook");
            mother.setDescription(message);
            mother.setType("main_post");
            mother.setIdSp(1L);
            mother.setIdPost(null);
            mother.setIdChild1(null);

            List<PostChild> children = new ArrayList<>();
            for (String mediaFbid : mediaFbids) {
                PostChild child = new PostChild();
                child.setPostUrl(postUrl);

                child.setMediaUrl("https://www.facebook.com/" + mediaFbid.split("__SEP__")[0]);
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
