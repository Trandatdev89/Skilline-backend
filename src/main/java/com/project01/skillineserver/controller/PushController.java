package com.project01.skillineserver.controller;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.dto.request.PushSubscription;
import com.project01.skillineserver.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/push")
public class PushController {


    @Value("${vapid.public.key}")
    private String publicKey;

    private final PushNotificationService pushNotificationService;


    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", publicKey));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(
            @AuthenticationPrincipal CustomUserDetail customUserDetail,
            @RequestParam Long userId,
            @RequestBody PushSubscription subscription) {

        Long userIdInUserDetail = customUserDetail.getUser().getId();
        pushNotificationService.subscribe(userIdInUserDetail, subscription);
        return ResponseEntity.ok("Subscribed successfully");
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe(
            @AuthenticationPrincipal CustomUserDetail customUserDetail,
            @RequestParam Long userId,
            @RequestBody PushSubscription subscription) {

        Long userIdInUserDetail = customUserDetail.getUser().getId();
        pushNotificationService.unsubscribe(userIdInUserDetail, subscription);
        return ResponseEntity.ok("Subscribed successfully");
    }

    // Gửi notification (API test)
    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(
            @AuthenticationPrincipal CustomUserDetail customUserDetail,
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String body) {

        Long userIdInUserDetail = customUserDetail.getUser().getId();
        pushNotificationService.sendNotification(userIdInUserDetail, title, body);
        return ResponseEntity.ok("Notification sent");
    }

    @GetMapping("/send-all-user")
    public ResponseEntity<String> sendNotificationForAllUser(
            @RequestParam String title,
            @RequestParam String body) {
        pushNotificationService.sendNotificationForAllUser(title, body);
        return ResponseEntity.ok("Notification sent");
    }
}
