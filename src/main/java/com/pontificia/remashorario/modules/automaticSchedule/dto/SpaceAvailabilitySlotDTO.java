package com.pontificia.remashorario.modules.automaticSchedule.dto;

import lombok.*;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpaceAvailabilitySlotDTO {
    private UUID spaceUuid;
    private String spaceName;
    private String spaceType; // THEORY, PRACTICE
    private UUID specialtyUuid;
    private String specialtyName;
    private Integer capacity;
    private DayOfWeek dayOfWeek;
    private UUID timeSlotUuid;
    private List<UUID> availableTeachingHourUuids;
    private Boolean isPreferred; // Si cumple con especialidad preferida
}

