package com.itu.socialcom.demo.storage;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseStorageService {
    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageService.class);

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket:post-media}")
    private String bucketName;

    private final OkHttpClient httpClient = new OkHttpClient();

    /**
     * Download media from a URL (e.g., Facebook) and upload to Supabase storage
     * @param sourceUrl The URL to download from (Facebook, Instagram, etc.)
     * @param fileName Optional custom filename (will generate UUID if null)
     * @return The public URL of the uploaded file in Supabase
     */
    public String downloadAndUploadToSupabase(String sourceUrl, String fileName) throws IOException {
        log.info("Downloading media from: {}", sourceUrl);

        // 1. Download the file from source URL
        byte[] fileData = downloadFile(sourceUrl);

        // 2. Generate filename if not provided
        if (fileName == null || fileName.isEmpty()) {
            String extension = extractFileExtension(sourceUrl);
            fileName = UUID.randomUUID() + extension;
        }

        // 3. Upload to Supabase
        return uploadToSupabase(fileData, fileName);
    }

    /**
     * Download file from URL as byte array
     */
    private byte[] downloadFile(String fileUrl) throws IOException {
        Request request = new Request.Builder()
                .url(fileUrl)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download file: " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }

            return body.bytes();
        }
    }

    /**
     * Upload byte array to Supabase storage
     */
    private String uploadToSupabase(byte[] fileData, String fileName) throws IOException {
        String uploadUrl = String.format("%s/storage/v1/object/%s/%s",
                supabaseUrl, bucketName, fileName);

        RequestBody requestBody = RequestBody.create(fileData, MediaType.parse("application/octet-stream"));

        Request request = new Request.Builder()
                .url(uploadUrl)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/octet-stream")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                throw new IOException("Failed to upload to Supabase: " + response.code() + " - " + errorBody);
            }

            // Return the public URL
            String publicUrl = String.format("%s/storage/v1/object/public/%s/%s",
                    supabaseUrl, bucketName, fileName);

            log.info("Successfully uploaded to Supabase: {}", publicUrl);
            return publicUrl;
        }
    }

    /**
     * Extract file extension from URL
     */
    private String extractFileExtension(String url) {
        // Check for common query parameters
        String cleanUrl = url.split("\\?")[0];

        if (cleanUrl.contains(".jpg") || cleanUrl.contains(".jpeg")) {
            return ".jpg";
        } else if (cleanUrl.contains(".png")) {
            return ".png";
        } else if (cleanUrl.contains(".gif")) {
            return ".gif";
        } else if (cleanUrl.contains(".mp4")) {
            return ".mp4";
        } else if (cleanUrl.contains(".webp")) {
            return ".webp";
        }

        // Default to jpg for images
        return ".jpg";
    }

    /**
     * Delete a file from Supabase storage
     */
    public boolean deleteFromSupabase(String fileName) throws IOException {
        String deleteUrl = String.format("%s/storage/v1/object/%s/%s",
                supabaseUrl, bucketName, fileName);

        Request request = new Request.Builder()
                .url(deleteUrl)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .delete()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Failed to delete from Supabase: {}", response.code());
                return false;
            }

            log.info("Successfully deleted from Supabase: {}", fileName);
            return true;
        }
    }

    /**
     * Extract filename from Supabase URL
     */
    public String extractFileNameFromUrl(String supabaseUrl) {
        if (supabaseUrl == null || !supabaseUrl.contains(bucketName)) {
            return null;
        }

        String[] parts = supabaseUrl.split(bucketName + "/");
        if (parts.length > 1) {
            return parts[1];
        }

        return null;
    }
}

