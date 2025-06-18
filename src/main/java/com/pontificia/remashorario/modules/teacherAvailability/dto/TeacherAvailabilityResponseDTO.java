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
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private boolean isAvailable;
    private String notes;

    // constructors, getters, setters
}
