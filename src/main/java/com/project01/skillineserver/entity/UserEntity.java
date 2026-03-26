package com.project01.skillineserver.entity;

import com.project01.skillineserver.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_username", columnList = "username")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_username", columnNames = "username")
        })
public class UserEntity extends BaseEntity<Long> {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String fullname;
    @Column(name = "avatar_asset_id")
    private String avatarAssetId;
    private String address;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean isLocked = false;
    private boolean isDisable = false;
    private boolean isAccountNonExpired = true;
    private boolean isCredentialsNonExpired = true;
    private Integer failedLoginAttempts = 0;
    private Instant lockTime;
    private Instant lastTimeChangePassword;
}
