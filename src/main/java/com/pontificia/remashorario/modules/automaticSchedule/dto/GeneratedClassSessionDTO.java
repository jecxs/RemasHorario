package com.pontificia.remashorario.modules.automaticSchedule.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class GeneratedClassSessionDTO {
    private UUID uuid;
    private String courseName;
    private String groupName;
    private String teacherName;
    private String learningSpaceName;
    private DayOfWeek dayOfWeek;
    private String timeSlotName;
    private List<String> teachingHourRanges; // ["07:00-07:45", "07:45-08:30"]
    private String sessionType; // THEORY, PRACTICE
    private String notes;
    private Boolean isNewlyGenerated; // Para distinguir de existentes
}

