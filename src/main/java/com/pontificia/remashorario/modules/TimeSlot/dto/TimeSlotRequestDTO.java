package com.pontificia.remashorario.modules.TimeSlot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class TimeSlotRequestDTO {
    @NotBlank(message = "El nombre del turno no puede estar vacío.")
    private String name;

    @NotNull(message = "La hora de inicio no puede ser nula.")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin no puede ser nula.")
    private LocalTime endTime;

    @NotNull(message = "La duración de la hora pedagógica no puede ser nula.")
    @Min(value = 1, message = "La duración de la hora pedagógica debe ser al menos 1 minuto.")
    private Integer pedagogicalHourDurationInMinutes;
}
