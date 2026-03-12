package com.project01.skillineserver.controller;

import com.project01.skillineserver.config.CustomUserDetail;
import com.project01.skillineserver.constants.SocketPrefix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
@Slf4j
@RequestMapping(value = "/api/test")
@RequiredArgsConstructor
public class TestController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping
    public ResponseEntity<?> test(@AuthenticationPrincipal CustomUserDetail userDetail) {
        Map<String, Object> infoUser = new HashMap<>();
        infoUser.put("name", "Tran Quoc Dat");
        infoUser.put("id", "123");
        simpMessagingTemplate.convertAndSend(SocketPrefix.NOTIFICATION + 1, infoUser);
        return ResponseEntity.ok().body(null);
    }

}
