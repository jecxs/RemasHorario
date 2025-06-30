package com.pontificia.remashorario.modules.automaticSchedule.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SchedulePreviewDTO {
    private List<GroupRequirementDTO> groupRequirements;
    private ScheduleConstraintsDTO constraints;
    private ScheduleFeasibilityDTO feasibility;
}

