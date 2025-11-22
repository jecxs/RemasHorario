package com.pontificia.remashorario.modules.teacherRate.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class TeacherRateRequestDTO {

    @NotNull(message = "El docente es obligatorio")
    private UUID teacherUuid;

    @NotNull(message = "El tipo de actividad es obligatorio")
    private UUID activityTypeUuid;

    @NotNull(message = "La tarifa por hora es obligatoria")
    @DecimalMin(value = "0.01", message = "La tarifa por hora debe ser mayor a cero")
    private BigDecimal ratePerHour;

    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}
