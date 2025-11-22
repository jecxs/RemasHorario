package com.pontificia.remashorario.modules.academicCalendarException.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AcademicCalendarExceptionRequestDTO {

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate date;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    private String code;

    private String description;
}
