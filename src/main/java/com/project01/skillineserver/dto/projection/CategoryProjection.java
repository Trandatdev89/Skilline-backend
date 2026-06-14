package com.project01.skillineserver.dto.projection;

import java.time.Instant;

public interface CategoryProjection {
    Long getId();

    String getName();

    String getSlug();

    Boolean getIsActive();

    String getPath();

    Instant getCreatedAt();

    Instant getUpdatedAt();
}
