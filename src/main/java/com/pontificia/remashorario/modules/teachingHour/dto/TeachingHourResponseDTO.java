package com.pontificia.remashorario.modules.teachingHour.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TeachingHourResponseDTO {
    private UUID uuid;
    private int orderInTimeSlot;
    private LocalTime startTime;
    private LocalTime endTime;
    private int durationMinutes;
}