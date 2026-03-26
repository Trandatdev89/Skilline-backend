package com.project01.skillineserver.entity;

import com.project01.skillineserver.enums.VariantType;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "media_asset_variant",
        indexes = {
                @Index(name = "idx_media_asset_variant_asset_id", columnList = "asset_id")
        })
public class MediaAssetVariantEntity extends UuidEntity<String> {

    @Column(name = "asset_id", nullable = false)
    private String assetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "variant_type", nullable = false, length = 30)
    private VariantType variantType;

    @Column(name = "bucket", nullable = false, length = 255)
    private String bucket;

    @Column(name = "object_key", nullable = false, length = 1024)
    private String objectKey;

    @Column(name = "mime_type", length = 150)
    private String mimeType;

    @Column(name = "width_px")
    private Integer widthPx;

    @Column(name = "height_px")
    private Integer heightPx;

    @Column(name = "bitrate")
    private Long bitrate;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
