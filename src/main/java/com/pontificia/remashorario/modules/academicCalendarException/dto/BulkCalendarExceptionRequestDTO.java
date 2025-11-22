package com.pontificia.remashorario.modules.academicCalendarException.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkCalendarExceptionRequestDTO {

    @NotEmpty(message = "La lista de excepciones no puede estar vacía")
    @Valid
    private List<AcademicCalendarExceptionRequestDTO> exceptions;
}
