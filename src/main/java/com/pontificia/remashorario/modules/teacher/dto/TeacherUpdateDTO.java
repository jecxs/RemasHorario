package com.pontificia.remashorario.modules.teacher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TeacherUpdateDTO {
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres")
    private String fullName;

    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
    private String phone;

    @NotNull(message = "El departamento es obligatorio")
    private UUID departmentUuid;

    private List<UUID> knowledgeAreaUuids = new ArrayList<>();
}
