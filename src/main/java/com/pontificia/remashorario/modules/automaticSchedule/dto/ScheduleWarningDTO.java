package com.pontificia.remashorario.modules.automaticSchedule.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;

@Getter
@Setter
@Builder
public class ScheduleWarningDTO {
    private String type; // TIME_GAP, UNEVEN_DISTRIBUTION, PREFERRED_SLOT_UNAVAILABLE
    private String message;
    private String severity;
    private String affectedGroup;
    private String suggestion;
    private DayOfWeek dayOfWeek;
}
