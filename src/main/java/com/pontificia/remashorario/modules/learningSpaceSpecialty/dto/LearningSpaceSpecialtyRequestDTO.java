package com.pontificia.remashorario.modules.learningSpaceSpecialty.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LearningSpaceSpecialtyRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;

    private UUID departmentUuid;
}
