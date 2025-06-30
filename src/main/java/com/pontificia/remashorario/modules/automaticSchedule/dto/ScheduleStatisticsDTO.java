package com.pontificia.remashorario.modules.automaticSchedule.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class ScheduleStatisticsDTO {
    private Map<String, Integer> sessionsPerDay;        // "MONDAY" -> 15
    private Map<String, Integer> sessionsPerTimeSlot;   // "M1" -> 25
    private Map<String, Double> teacherUtilization;     // "Teacher UUID" -> 0.85
    private Map<String, Double> spaceUtilization;       // "Space UUID" -> 0.70
    private Map<String, Integer> hoursPerCourse;        // "Course Name" -> 6
    private Double averageHoursPerDay;
    private Double distributionBalance; // Qué tan equilibrada está la distribución (0-1)
}