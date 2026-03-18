package com.project01.skillineserver.controller;

import com.project01.skillineserver.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@Slf4j
@RequestMapping(value = "/api/test")
@RequiredArgsConstructor
public class TestController {

    private final S3Service service;

    @PostMapping
    public ResponseEntity<?> test(@ModelAttribute MultipartFile file) throws IOException {
        String key = service.uploadFile(file);
        return ResponseEntity.ok().body(key);
    }

    @GetMapping
    public ResponseEntity<?> getImage(@RequestParam String key) throws IOException {
        String image = service.getFileUrl(key);
        return ResponseEntity.ok().body(image);
    }

}
