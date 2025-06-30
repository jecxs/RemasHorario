package com.pontificia.remashorario.modules.automaticSchedule.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExistingScheduleAnalysisDTO {
    private List<GroupScheduleStatusDTO> groupStatuses;
    private Integer totalGroups;
    private Integer groupsWithExistingSessions;
    private Integer groupsWithoutSessions;
    private Boolean needsUserDecision;
    private List<String> recommendations;
    private WorkloadAnalysisDTO workloadAnalysis;
}