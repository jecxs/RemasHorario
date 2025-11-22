package com.pontificia.remashorario.modules.extraAssignment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class ExtraAssignmentRequestDTO {

    @NotNull(message = "El docente es obligatorio")
    private UUID teacherUuid;

    @NotNull(message = "El tipo de actividad es obligatorio")
    private UUID activityTypeUuid;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no puede exceder 255 caracteres")
    private String title;

    @NotNull(message = "La fecha de asignación es obligatoria")
    private LocalDate assignmentDate;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;

    @DecimalMin(value = "0.01", message = "La tarifa por hora debe ser mayor a cero")
    private BigDecimal ratePerHour;

    private String notes;
}
