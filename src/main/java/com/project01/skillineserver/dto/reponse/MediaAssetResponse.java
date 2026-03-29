package com.project01.skillineserver.dto.reponse;


import com.project01.skillineserver.enums.AssetType;
import com.project01.skillineserver.enums.PlaybackType;
import com.project01.skillineserver.enums.ProcessStatus;
import com.project01.skillineserver.enums.UploadStatus;
import lombok.Builder;

@Builder
public record MediaAssetResponse(
        String assetId,
        AssetType assetType,
        String publicUrl,       // URL CloudFront để truy cập (ảnh / HLS master)
        String mimeType,
        Long sizeBytes,
        Integer widthPx,
        Integer heightPx,
        Long durationSeconds,
        PlaybackType playbackType,
        UploadStatus uploadStatus,
        ProcessStatus processStatus
) {
}
