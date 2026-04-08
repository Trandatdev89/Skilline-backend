package com.project01.skillineserver.entity;

import com.project01.skillineserver.enums.EmailType;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "email_template")
public class EmailTemplate extends BaseEntity<Long> {

    @Enumerated(EnumType.STRING)
    private EmailType type;
    private String subject;
    @Column(columnDefinition = "TEXT")
    private String htmlContent;
    private String language;
    private boolean active;
}
