package com.project01.skillineserver.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record CategoryReq(Long id,
                          String name,
                          MultipartFile path,
                          String slug) {
}
