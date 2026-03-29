package com.project01.skillineserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_subscriptions")
public class UserSubscription extends BaseEntity<Long> {

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 1000)
    private String endpoint;

    private String p256dh;
    private String auth;
}
