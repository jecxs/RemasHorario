package com.pontificia.remashorario.modules.teacherAttendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class TeacherAttendanceCheckInRequestDTO {

    @NotNull(message = "El docente es obligatorio")
    private UUID teacherUuid;

    @NotNull(message = "La sesión de clase es obligatoria")
    private UUID classSessionUuid;

    private LocalDate attendanceDate;
}
