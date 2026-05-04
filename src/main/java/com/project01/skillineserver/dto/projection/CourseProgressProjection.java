package com.project01.skillineserver.dto.projection;

public interface CourseProgressProjection {
    Long progressPercent();
    boolean isCompleted();
    String completeDate();
}
