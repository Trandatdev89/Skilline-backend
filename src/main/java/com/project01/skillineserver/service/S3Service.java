package com.project01.skillineserver.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3Service {
    String uploadPublicFile(MultipartFile file) throws IOException;   // ảnh

    String uploadPrivateFile(MultipartFile file) throws IOException;  // video

    String getFileUrl(String key);

    String generatePresignedUrl(String key, long expirationMinutes);

    void deleteFile(String key);
}
