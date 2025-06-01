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

    @NotBlank(message = "El código del curso es obligatorio")
    private String code;

    @NotNull(message = "El ciclo es obligatorio")
    private UUID cycleUuid;

    @NotNull(message = "El área de conocimiento es obligatoria")
    private UUID knowledgeAreaUuid;

    @NotNull(message = "Las horas teóricas semanales son obligatorias")
    @Min(value = 0, message = "Debe ser 0 o mayor")
    private Integer weeklyTheoryHours;

    @NotNull(message = "Las horas prácticas semanales son obligatorias")
    @Min(value = 0, message = "Debe ser 0 o mayor")
    private Integer weeklyPracticeHours;

    private UUID preferredSpecialtyUuid;

    @NotEmpty(message = "Debe seleccionar al menos un tipo de enseñanza")
    private List<UUID> teachingTypeUuids;
}
