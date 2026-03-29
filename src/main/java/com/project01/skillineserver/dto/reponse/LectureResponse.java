package com.project01.skillineserver.dto.reponse;

import com.project01.skillineserver.enums.PublishStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LectureResponse {
    private String id;
    private Integer position;
    private String title;
    private String urlThumbnail;
    private String createAt;
    private String updateAt;
    private String urlVideo;
    private Long durationSeconds;
    private boolean previewable;
    private PublishStatus publishStatus;
}
