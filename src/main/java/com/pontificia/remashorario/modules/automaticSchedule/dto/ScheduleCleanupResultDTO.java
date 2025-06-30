package com.pontificia.remashorario.modules.automaticSchedule.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleCleanupResultDTO {
    private Boolean success;
    private String message;
    private Integer deletedSessions;
    private Integer affectedGroups;
    private Integer affectedCourses;
    private String cleanupStrategy;
    private List<String> warnings;
    private Map<String, Object> details;
}