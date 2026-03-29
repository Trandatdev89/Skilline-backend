package com.project01.skillineserver.dto.request;

public record ConfirmUploadRequest(
        String assetId   // UUID trả về từ initUpload
) {
}