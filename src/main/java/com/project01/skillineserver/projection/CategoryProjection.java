package com.project01.skillineserver.projection;

import java.time.Instant;

public interface CategoryProjection {
    Long getId();

    String getName();

    String getSlug();

    Boolean isActive();

    String getObjectKey();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    Long getCreatedBy();

    Long getUpdateBy();
}
