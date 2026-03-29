package com.project01.skillineserver.service;

import jakarta.servlet.http.HttpServletResponse;

public interface CloudFrontService {
    /**
     * Tạo CloudFront Signed Cookie cho toàn bộ path của course.
     * Cookie có thời hạn 8 tiếng (1 session xem video).
     */
    void setSignedCookieForCourse(Long courseId, Long userId, HttpServletResponse response);

    /**
     * Tạo CloudFront Signed URL cho một file cụ thể (ảnh private).
     */
    String generateSignedUrl(String objectKey, int expiresInSeconds);
}
