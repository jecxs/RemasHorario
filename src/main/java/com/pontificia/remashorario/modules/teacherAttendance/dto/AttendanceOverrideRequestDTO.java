package com.pontificia.remashorario.modules.teacherAttendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AttendanceOverrideRequestDTO {

    @NotNull(message = "La hora de entrada es obligatoria")
    private LocalDateTime checkinAt;

    @NotNull(message = "La hora de salida es obligatoria")
    private LocalDateTime checkoutAt;

    private Boolean resetPenalties = false;

    private String adminNote;
}
