package com.pontificia.remashorario.modules.automaticSchedule.dto;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupScheduleStatusDTO {
    private UUID groupUuid;
    private String groupName;
    private Boolean hasExistingSessions;
    private Integer sessionCount;
    private Integer totalAssignedHours;
    private Integer assignedCoursesCount;
    private Integer assignedTeachersCount;
    private Double estimatedCompleteness; // 0.0 a 1.0
    private Map<String, Integer> distributionByDay; // "MONDAY" -> 3
    private String recommendedAction; // "RESET", "COMPLETE", "NONE"
}
