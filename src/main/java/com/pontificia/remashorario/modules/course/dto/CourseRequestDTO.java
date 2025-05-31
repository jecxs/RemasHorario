package com.pontificia.remashorario.modules.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CourseRequestDTO {
    @NotBlank(message = "El nombre del curso es obligatorio")
    private String name;

    @NotNull(message = "Las horas semanales son obligatorias")
    @Min(value = 1, message = "El curso debe tener al menos 1 hora semanal")
    private Integer weeklyHours;

    @NotNull(message = "El ciclo es obligatorio")
    private UUID cycleUuid;

    @NotNull(message = "El departamento académico es obligatorio")
    private UUID departmentUuid;

    @NotEmpty(message = "Debe seleccionar al menos un tipo de enseñanza")
    private List<UUID> teachingTypeUuids;
}
