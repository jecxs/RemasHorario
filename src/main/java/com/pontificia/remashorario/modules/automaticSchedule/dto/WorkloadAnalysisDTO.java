package com.pontificia.remashorario.modules.automaticSchedule.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkloadAnalysisDTO {
    private Double averageTeacherLoad;
    private Double averageSpaceLoad;
    private Double systemUtilization;
    private List<String> overloadedTeachers;
    private List<String> overloadedSpaces;
    private List<String> recommendations;
    private String utilizationLevel; // "LOW", "MEDIUM", "HIGH", "CRITICAL"
}
