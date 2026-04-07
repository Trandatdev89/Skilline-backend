package com.project01.skillineserver.projection;

import java.time.Instant;

public interface CategoryProjection {
    String getId();

    String getName();

    String getSlug();

    Boolean isActive();

    String getObjectKey();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    String getCreatedBy();

    String getUpdateBy();
}
