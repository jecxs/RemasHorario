package com.pontificia.remashorario.modules.automaticSchedule.dto;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleSlotDTO {
    private DayOfWeek dayOfWeek;
    private UUID timeSlotUuid;
    private String timeSlotName;
    private List<UUID> teachingHourUuids;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;
    private Boolean isPreferred; // Si está en lista de turnos preferentes
}
