package com.project01.skillineserver.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AuditCommonRes {
    private String createdAt;
    private String updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
