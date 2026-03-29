package com.project01.skillineserver.kafka.event;

import com.project01.skillineserver.enums.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadedEvent {
    private String assetId;
    private AssetType assetType;
    private String bucket;
    private String objectKey;
    private String mimeType;
    private Long sizeBytes;
}