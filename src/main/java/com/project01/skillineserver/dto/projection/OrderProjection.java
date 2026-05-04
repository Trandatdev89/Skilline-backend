package com.project01.skillineserver.dto.projection;

import java.math.BigDecimal;
import java.time.Instant;

public interface OrderProjection {
    String getId();
    String getStatus();
    Instant getCreatedAt();
    BigDecimal getTotalPrice();
    String getUsername();
    String getAddress();
    String getFullname();
    String getEmail();
    String getPhone();
}
