package com.pontificia.remashorario.modules.teacherAttendance.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class TeacherAttendanceCheckInWithScheduleRequestDTO {

    @NotNull(message = "El docente es obligatorio")
    private UUID teacherUuid;

    @NotNull(message = "La sesión de clase es obligatoria")
    private UUID classSessionUuid;

    private LocalDate attendanceDate;

    @NotNull(message = "La hora programada de inicio es obligatoria")
    private LocalTime scheduledStartTime;

    @NotNull(message = "La hora programada de fin es obligatoria")
    private LocalTime scheduledEndTime;

    @NotNull(message = "La duración programada es obligatoria")
    @Min(value = 1, message = "La duración debe ser al menos 1 minuto")
    private Integer scheduledDurationMinutes;
}
