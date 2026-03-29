package com.project01.skillineserver.dto.reponse;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InitUploadResponse {
    private String assetId;
    private String objectKey;
    private String uploadUrl;
}
