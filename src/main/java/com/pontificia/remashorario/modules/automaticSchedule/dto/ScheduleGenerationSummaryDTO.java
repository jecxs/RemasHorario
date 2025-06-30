package com.pontificia.remashorario.modules.automaticSchedule.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ScheduleGenerationSummaryDTO {
    private Integer totalGroupsProcessed;
    private Integer totalCoursesProcessed;
    private Integer totalSessionsGenerated;
    private Integer totalHoursAssigned;
    private Integer conflictsFound;
    private Integer warningsGenerated;
    private Double successRate; // Porcentaje de éxito
}
