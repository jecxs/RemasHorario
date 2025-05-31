package com.pontificia.remashorario.modules.teachingHour.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class TeachingHourRequestDTO {

    @NotNull(message = "Time slot ID is required")
    private UUID timeSlotId;

    @Min(value = 1, message = "Order must be at least 1")
    private int orderInShift;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 180, message = "Duration cannot exceed 180 minutes")
    private int duration;
}

