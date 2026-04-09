package com.project01.skillineserver.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentEvent {
    private Long userId;
    private List<Long> coursesId;
}
