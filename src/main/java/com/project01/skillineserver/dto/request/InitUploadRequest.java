package com.project01.skillineserver.dto.request;

import com.project01.skillineserver.enums.AssetType;

public record InitUploadRequest(
        AssetType assetType,       // IMAGE hoặc VIDEO
        String originalFileName,   // tên file gốc
        String mimeType,           // image/jpeg, video/mp4...
        Long sizeBytes             // kích thước file (bytes)
) {
}