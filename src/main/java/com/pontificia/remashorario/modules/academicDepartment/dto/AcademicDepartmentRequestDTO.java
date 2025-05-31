package com.pontificia.remashorario.modules.academicDepartment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcademicDepartmentRequestDTO {
    @NotBlank(message = "El nombre del departamento es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String name;

    @Size(max = 10, message = "El código no puede exceder los 10 caracteres")
    private String code;

    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    private String description;
}
