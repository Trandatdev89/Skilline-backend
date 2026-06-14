package com.project01.skillineserver.dto.reponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse extends AuditCommonRes {
    private Long id;
    private String name;
    private String urlThumbnail;
    private boolean isActive;
    private String slug;
}