package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.properties.CdnProperties;
import com.project01.skillineserver.service.CloudFrontService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;

import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudFrontServiceImpl implements CloudFrontService {

    private final CdnProperties cdnProperties;
    // Cookie valid 8 tiếng - đủ cho 1 session học
    private static final int COOKIE_TTL_SECONDS = 8 * 3600;

    @Override
    public void setSignedCookieForCourse(Long courseId, Long userId, HttpServletResponse response) {
        try {
            /*
             * CloudFront Signed Cookie cho phép truy cập toàn bộ path:
             * https://cdn.example.com/videos/hls/*
             *
             * Cách hoạt động:
             * 1. Server tạo 3 cookie: CloudFront-Policy, CloudFront-Signature, CloudFront-Key-Pair-Id
             * 2. Browser tự đính kèm cookie vào mọi request đến CloudFront domain
             * 3. CloudFront verify cookie trước khi serve HLS segments
             *
             * Dùng AWS SDK v2 CloudFrontUtilities để tạo signed cookies:
             */
            String resourceUrl = cdnProperties.getDomain() + "/videos/hls/*";
            Instant expiry = Instant.now().plus(COOKIE_TTL_SECONDS, ChronoUnit.SECONDS);

            CloudFrontUtilities utilities = CloudFrontUtilities.create();

            CannedSignerRequest signerRequest = CannedSignerRequest.builder()
                    .resourceUrl(resourceUrl)
                    .privateKey(Path.of(cdnProperties.getPrivateKeyPath()))
                    .keyPairId(cdnProperties.getKeyPairId())
                    .expirationDate(expiry)
                    .build();

            // Tạo signed URL để extract policy/signature
            SignedUrl signedUrl = utilities.getSignedUrlWithCannedPolicy(signerRequest);

            // Parse và set cookies từ signed URL
            // CloudFront cần 3 cookies: Policy, Signature, Key-Pair-Id
            URL url = new URL(signedUrl.url());
            String query = url.getQuery();

            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2) {
                    Cookie cookie = new Cookie("CloudFront-" + kv[0], kv[1]);
                    cookie.setHttpOnly(true);
                    cookie.setSecure(true);
                    cookie.setPath("/");
                    cookie.setDomain(cdnProperties.getCookieDomain());
                    cookie.setMaxAge(COOKIE_TTL_SECONDS);
                    response.addCookie(cookie);
                }
            }

            log.info("Set CloudFront signed cookie for user [{}] course [{}]", userId, courseId);

        } catch (Exception e) {
            log.error("Failed to create CloudFront signed cookie: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_SERVER);
        }
    }

    @Override
    public String generateSignedUrl(String objectKey, int expiresInSeconds) {
        try {
            CloudFrontUtilities utilities = CloudFrontUtilities.create();
            String resourceUrl = cdnProperties.getDomain() + "/" + objectKey;

            CannedSignerRequest signerRequest = CannedSignerRequest.builder()
                    .resourceUrl(resourceUrl)
                    .privateKey(Path.of(cdnProperties.getPrivateKeyPath()))
                    .keyPairId(cdnProperties.getKeyPairId())
                    .expirationDate(Instant.now().plusSeconds(expiresInSeconds))
                    .build();

            return utilities.getSignedUrlWithCannedPolicy(signerRequest).url();

        } catch (Exception e) {
            log.error("Failed to generate signed URL for [{}]: {}", objectKey, e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER);
        }
    }
}
