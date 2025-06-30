package com.pontificia.remashorario.modules.automaticSchedule.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ScheduleConstraintsDTO {
    private Integer totalGroups;
    private Integer totalCourses;
    private Integer totalRequiredHours;
    private Integer availableTeachers;
    private Integer availableSpaces;
    private Integer availableTimeSlots;
    private List<String> potentialConstraints; // Posibles limitaciones
}

