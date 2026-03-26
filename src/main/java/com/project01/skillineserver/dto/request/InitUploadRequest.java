package com.project01.skillineserver.dto.request;

import com.project01.skillineserver.enums.AssetType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitUploadRequest {
    private AssetType assetType;
    private String fileName;
    private String mimeType;
    private Long sizeBytes;
    private Long courseId;
    private String lectureId;
    private Boolean isPrivate;
}