package com.pontificia.remashorario.modules.automaticSchedule.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ScheduleFeasibilityDTO {
    private Boolean isFeasible;
    private Double feasibilityScore; // 0-1
    private List<String> challenges;
    private List<String> recommendations;
}
