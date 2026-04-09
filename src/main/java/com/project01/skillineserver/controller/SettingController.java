package com.project01.skillineserver.controller;

import com.project01.skillineserver.dto.ApiResponse;
import com.project01.skillineserver.dto.request.TemplateMailReq;
import com.project01.skillineserver.entity.EmailTemplate;
import com.project01.skillineserver.enums.EmailType;
import com.project01.skillineserver.service.TemplateMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/config")
public class SettingController {

    private final TemplateMailService templateMailService;

    @PostMapping(value = "/save-template-mail")
    @PreAuthorize("@authorizationService.isAdmin()")
    public ApiResponse<?> saveTemplateMail(@RequestBody TemplateMailReq templateMailReq){
        templateMailService.saveTemplateMail(templateMailReq);
        return ApiResponse.builder()
                .message("Save template done")
                .code(200)
                .build();
    }

    @GetMapping(value = "/get-template-mail")
    @PreAuthorize("@authorizationService.isAdmin()")
    public ApiResponse<EmailTemplate> getTemplateMail(@RequestParam EmailType emailType) {
        return ApiResponse.<EmailTemplate>builder()
                .message("Get template mail done !")
                .data(templateMailService.getTemplateMail(emailType))
                .code(200)
                .build();
    }



}
