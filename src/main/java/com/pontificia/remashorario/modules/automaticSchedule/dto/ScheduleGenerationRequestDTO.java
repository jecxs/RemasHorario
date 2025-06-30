package com.pontificia.remashorario.modules.automaticSchedule.dto;


import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleGenerationRequestDTO {

    @NotNull(message = "El periodo es obligatorio")
    private UUID periodUuid;

    // Filtros de alcance (al menos uno debe estar presente)
    private UUID modalityUuid;      // Generar para toda la modalidad
    private UUID careerUuid;        // Generar para toda la carrera
    private UUID cycleUuid;         // Generar para un ciclo específico
    private List<UUID> groupUuids;  // Generar para grupos específicos

    // Filtros de configuración
    private List<DayOfWeek> excludedDays = new ArrayList<>();  // Días a evitar
    private List<UUID> preferredTimeSlotUuids = new ArrayList<>();  // Turnos preferentes
    private Integer maxHoursPerDay = 8;           // Máximo de horas por día
    private Integer minHoursPerDay = 2;           // Mínimo de horas por día
    private Boolean distributeEvenly = true;      // Distribuir equitativamente en la semana
    private Boolean respectTeacherContinuity = true; // Mantener mismo docente por curso
    private Boolean avoidTimeGaps = true;         // Evitar huecos de tiempo

    // Configuración avanzada
    private Integer maxConsecutiveHours = 4;      // Máximo de horas consecutivas del mismo curso
    private Boolean prioritizeLabsAfterTheory = false; // Priorizar práctica después de teoría
    private Double preferredTimeSlotWeight = 0.7; // Peso para turnos preferentes (0-1)

    // Validación
    @AssertTrue(message = "Debe especificar al menos un filtro de alcance")
    public boolean isValidScope() {
        return modalityUuid != null || careerUuid != null ||
                cycleUuid != null || (groupUuids != null && !groupUuids.isEmpty());
    }
}

