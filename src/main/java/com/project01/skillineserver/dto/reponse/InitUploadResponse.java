package com.project01.skillineserver.dto.reponse;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InitUploadResponse {
    private String assetId;
    private String bucket;
    private String objectKey;
    private String uploadUrl;
    private Instant expiresAt;
}
