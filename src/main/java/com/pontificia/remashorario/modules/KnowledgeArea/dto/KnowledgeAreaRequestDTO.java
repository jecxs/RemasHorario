package com.pontificia.remashorario.modules.KnowledgeArea.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class KnowledgeAreaRequestDTO {
    @NotBlank(message = "El nombre del área de conocimiento es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String name;

    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    private String description;

    @NotNull(message = "El departamento es obligatorio")
    private UUID departmentUuid;
}
