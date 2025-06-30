package com.pontificia.remashorario.modules.automaticSchedule.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseRequirementDTO {
    private UUID courseUuid;
    private String courseName;
    private UUID cycleUuid;
    private Integer totalTheoryHours;
    private Integer totalPracticeHours;
    private Integer totalHours;
    private UUID requiredKnowledgeAreaUuid;
    private UUID preferredSpecialtyUuid;
    private List<UUID> supportedTeachingTypeUuids;
    private Boolean isMixed; // Teoría + Práctica
}
