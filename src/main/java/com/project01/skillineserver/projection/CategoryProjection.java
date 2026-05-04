package com.project01.skillineserver.projection;

import java.time.Instant;

public interface CategoryProjection {
    Long getId();

    String getName();

    String getSlug();

    Boolean getIsActive();

    String getThumbnailAssetId();

    Instant getCreatedAt();

    Instant getUpdatedAt();
}
