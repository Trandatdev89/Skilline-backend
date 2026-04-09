package com.project01.skillineserver.controller;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.dto.reponse.InitUploadResponse;
import com.project01.skillineserver.dto.reponse.MediaAssetResponse;
import com.project01.skillineserver.dto.request.ConfirmUploadRequest;
import com.project01.skillineserver.dto.request.InitUploadRequest;
import com.project01.skillineserver.service.CloudFrontService;
import com.project01.skillineserver.service.MediaService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final CloudFrontService cloudFrontService;

    /**
     * Bước 1: Lấy presigned URL để upload.
     * Frontend gọi API này trước, nhận về uploadUrl + assetId.
     * <p>
     * POST /api/media/upload/init
     * Body: { "assetType": "VIDEO", "originalFileName": "lecture.mp4",
     * "mimeType": "video/mp4", "sizeBytes": 104857600 }
     */
    @PostMapping("/upload/init")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<InitUploadResponse> initUpload(
            @RequestBody InitUploadRequest request) {

        return ApiResponse.<InitUploadResponse>builder()
                .code(200)
                .message("Presigned URL created. Upload file to uploadUrl within 15 minutes.")
                .data(mediaService.initUploadFile(request))
                .build();
    }

    /**
     * Bước 2: Xác nhận đã upload xong.
     * Frontend gọi sau khi PUT file thành công lên S3.
     * Server sẽ publish Kafka event để xử lý async (transcode HLS...).
     * <p>
     * POST /api/media/upload/confirm
     * Body: { "assetId": "550e8400-e29b-41d4-a716-446655440000" }
     */
    @PostMapping("/upload/confirm")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<MediaAssetResponse> confirmUpload(
            @RequestBody ConfirmUploadRequest request) {

        return ApiResponse.<MediaAssetResponse>builder()
                .code(200)
                .message("Upload confirmed. Processing started.")
                .data(mediaService.confirmUpload(request))
                .build();
    }

    /**
     * Lấy thông tin asset (URL, trạng thái transcode...).
     * Frontend dùng để poll trạng thái xử lý video.
     * <p>
     * GET /api/media/{assetId}
     */
    @GetMapping("/{assetId}")
    public ApiResponse<MediaAssetResponse> getAsset(@PathVariable String assetId) {
        return ApiResponse.<MediaAssetResponse>builder()
                .code(200)
                .message("Success")
                .data(mediaService.getAssetById(assetId))
                .build();
    }

    /**
     * Set CloudFront Signed Cookie để xem video private.
     * User đã enrolled mới được xem video bài giảng.
     * <p>
     * GET /api/media/stream/access?courseId=1
     * <p>
     * Sau khi gọi API này, browser có cookie CloudFront-Policy,
     * CloudFront-Signature, CloudFront-Key-Pair-Id → dùng để
     * truy cập HLS playlist và segments qua CloudFront.
     */
    @GetMapping("/stream/access")
    @PreAuthorize("@authorizationService.isCanAccessApi()")
    public ApiResponse<Void> getStreamAccess(
            @RequestParam Long courseId,
            @AuthenticationPrincipal CustomUserDetail userDetail,
            HttpServletResponse response) {

        cloudFrontService.setSignedCookieForCourse(
                courseId, userDetail.getUser().getId(), response);

        return ApiResponse.<Void>builder()
                .code(200)
                .message("Stream access granted.")
                .build();
    }
}