package com.project01.skillineserver.dto.reponse;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CategoryResponse extends AuditCommonRes {
    private String id;
    private String name;
    private String urlThumbnail;
    private boolean isActive;
    private String slug;
}