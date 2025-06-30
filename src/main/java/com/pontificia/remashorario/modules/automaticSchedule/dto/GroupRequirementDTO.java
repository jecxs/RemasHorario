package com.pontificia.remashorario.modules.automaticSchedule.dto;


import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupRequirementDTO {
    private UUID groupUuid;
    private String groupName;
    private UUID cycleUuid;
    private UUID periodUuid;
    private List<CourseRequirementDTO> courses;
    private Integer totalWeeklyHours; // Suma de todas las horas de todos los cursos
}
