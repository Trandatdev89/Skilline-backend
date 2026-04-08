package com.project01.skillineserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "user_devices")
public class UserDevice extends BaseEntity<Long> {

    private Long userId;

    @Column(unique = true)
    private String deviceId;

    private String ipAddress; // Lưu IP để tham khảo thôi

    private String userAgent;  // Lưu để hiển thị cho user biết

    private Instant firstLogin;

    private Instant lastLogin;

    private boolean isActive;
}
