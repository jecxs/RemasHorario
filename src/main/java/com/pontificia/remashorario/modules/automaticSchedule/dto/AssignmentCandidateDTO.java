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
public class AssignmentCandidateDTO {
    private UUID groupUuid;
    private UUID courseUuid;
    private UUID teacherUuid;
    private UUID spaceUuid;
    private DayOfWeek dayOfWeek;
    private UUID timeSlotUuid;
    private List<UUID> teachingHourUuids;
    private String sessionType; // THEORY, PRACTICE
    private Double score; // Puntuación de qué tan buena es esta asignación
    private String scoreExplanation; // Explicación de la puntuación
}
