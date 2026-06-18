package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.dto.reponse.NotificationResponse;
import com.project01.skillineserver.dto.request.NotificationReq;
import com.project01.skillineserver.enums.NotificationType;
import com.project01.skillineserver.mapper.NotificationMapper;
import com.project01.skillineserver.repository.NotificationRepository;
import com.project01.skillineserver.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationRepository notificationRepository;

    @Override
    public List<NotificationResponse> getListNotification(NotificationType notificationType) {

        switch (notificationType) {
            case NotificationType.MY_SELF -> {
                return getNotificationMySelf();
            }
            case NotificationType.SYSTEM -> {
                return getNotificationSystem();
            }
            case NotificationType.USER_OTHER -> {
                return getNotificationUserOther();
            }
            default -> {
                return getAllNotification();
            }
        }
    }

    @Override
    public void createNotification(NotificationReq notificationReq) {

    }

    @Override
    public void inActiveNotification() {

    }

    private List<NotificationResponse> getAllNotification() {
        return null;
    }

    private List<NotificationResponse> getNotificationSystem() {
        return null;
    }

    private List<NotificationResponse> getNotificationMySelf() {
        return null;
    }

    private List<NotificationResponse> getNotificationUserOther() {
        return null;
    }
}
