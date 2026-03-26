package com.project01.skillineserver.controller;

import com.project01.skillineserver.dto.reponse.InitUploadResponse;
import com.project01.skillineserver.dto.request.InitUploadRequest;
import com.project01.skillineserver.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/file")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/init-upload")
    public ResponseEntity<InitUploadResponse> initUploadFile(@RequestBody InitUploadRequest initUploadRequest) {
        return ResponseEntity.ok(mediaService.initUploadFile(initUploadRequest));
    }

}
