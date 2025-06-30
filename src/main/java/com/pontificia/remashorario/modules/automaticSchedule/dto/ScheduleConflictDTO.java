package com.pontificia.remashorario.modules.automaticSchedule.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.List;

@Getter
@Setter
@Builder
public class ScheduleConflictDTO {
    private String type; // TEACHER_CONFLICT, SPACE_CONFLICT, GROUP_CONFLICT
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private String description;
    private String affectedCourse;
    private String affectedGroup;
    private String affectedTeacher;
    private String affectedSpace;
    private DayOfWeek dayOfWeek;
    private String timeRange;
    private List<String> suggestedSolutions;
}
