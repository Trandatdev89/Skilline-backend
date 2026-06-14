package com.project01.skillineserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "category")
public class CategoryEntity extends BaseEntity<Long> {

    private String name;

    @Column(name = "slug", unique = true)
    private String slug;

    private String path;

    private boolean isActive;
}
