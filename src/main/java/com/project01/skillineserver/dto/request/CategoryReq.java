package com.project01.skillineserver.dto.request;

public record CategoryReq(Long id,
                          String name,
                          String assetId,
                          String slug) {
}
