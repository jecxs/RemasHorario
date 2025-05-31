package com.pontificia.remashorario.modules.TimeSlot.dto;


import com.pontificia.remashorario.modules.teachingHour.dto.TeachingHourResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TimeSlotResponseDTO {
    private UUID uuid;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<TeachingHourResponseDTO> teachingHours;
}