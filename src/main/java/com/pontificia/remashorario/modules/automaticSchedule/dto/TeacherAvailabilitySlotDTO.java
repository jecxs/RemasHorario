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
public class TeacherAvailabilitySlotDTO {
    private UUID teacherUuid;
    private String teacherName;
    private DayOfWeek dayOfWeek;
    private UUID timeSlotUuid;
    private String timeSlotName;
    private List<UUID> availableTeachingHourUuids;
    private List<UUID> knowledgeAreaUuids;
    private Boolean isPreferred; // Si está en turno preferente
}
