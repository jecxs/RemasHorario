package com.pontificia.remashorario.modules.studentGroup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StudentGroupRequestDTO {

    @NotBlank(message = "El nombre del grupo es obligatorio")
    @Size(max = 10, message = "El nombre del grupo no debe exceder los 10 caracteres")
    private String name;

    @NotNull(message = "El UUID del ciclo es obligatorio")
    private UUID cycleUuid;

    @NotNull(message = "El UUID del periodo es obligatorio")
    private UUID periodUuid;
}
