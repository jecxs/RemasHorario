package com.pontificia.remashorario.modules.classSession.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ClassSessionRequestDTO {
    @NotNull(message = "El grupo de estudiantes es obligatorio")
    private UUID studentGroupUuid;

    @NotNull(message = "El curso es obligatorio")
    private UUID courseUuid;

    @NotNull(message = "El docente es obligatorio")
    private UUID teacherUuid;

    @NotNull(message = "El espacio de aprendizaje es obligatorio")
    private UUID learningSpaceUuid;

    @NotNull(message = "El día de la semana es obligatorio")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "El tipo de sesión es obligatorio")
    private UUID sessionTypeUuid;

    @NotEmpty(message = "Debe seleccionar al menos una hora pedagógica")
    private List<UUID> teachingHourUuids;

    @Size(max = 500, message = "Las notas no pueden exceder los 500 caracteres")
    private String notes;
}

