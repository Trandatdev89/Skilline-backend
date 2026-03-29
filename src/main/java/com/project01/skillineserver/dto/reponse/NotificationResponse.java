package com.project01.skillineserver.dto.reponse;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {
    private String id;
    private String content;
    private String nameUser;
    private String avatarUrl;
    private String title;
    private String timePush;
    private String createdAt;
    private String linkAttachment;
    private boolean isActive;
    private String readAt;
}
