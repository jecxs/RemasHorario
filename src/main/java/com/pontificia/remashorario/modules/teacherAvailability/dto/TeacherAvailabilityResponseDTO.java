package com.pontificia.remashorario.modules.teacherAvailability.dto;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TeacherAvailabilityResponseDTO {
    private UUID uuid;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}