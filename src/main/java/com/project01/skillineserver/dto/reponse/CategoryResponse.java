package com.project01.skillineserver.dto.reponse;

import lombok.Builder;

@Builder
public record CategoryResponse(Long id,String name,String urlThumbnail,boolean isActive) {
}
