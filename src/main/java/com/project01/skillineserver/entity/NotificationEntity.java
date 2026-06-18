package com.project01.skillineserver.entity;

import com.project01.skillineserver.enums.NotificationType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
@Entity
@Table(name = "notification")
public class NotificationEntity extends UuidEntity<String> {

    private NotificationType notificationType;
    private String content;
    private Long userId;
    private String nameUser;
    private String title;
    private String linkAttachment;
    private Instant timePush;
    private boolean isRead = false;        // đã đọc chưa
    private Instant readAt;                // đọc lúc nào
    private boolean isDeleted = false;     // soft delete

}
