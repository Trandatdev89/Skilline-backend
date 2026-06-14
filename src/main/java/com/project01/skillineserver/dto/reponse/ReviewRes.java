package com.project01.skillineserver.dto.reponse;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewRes {
    private Long id;
    private Integer rating;
    private String avatar;
    private Long userId;
    private Long courseId;
    private String comment;
    private String username;
    private String createdAt;
}
