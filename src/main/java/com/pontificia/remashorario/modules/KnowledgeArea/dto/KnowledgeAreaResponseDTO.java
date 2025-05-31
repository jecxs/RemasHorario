package com.pontificia.remashorario.modules.KnowledgeArea.dto;

import com.pontificia.remashorario.modules.academicDepartment.dto.AcademicDepartmentResponseDTO;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
public class KnowledgeAreaResponseDTO {
    private UUID uuid;
    private String name;
    private String description;
    private AcademicDepartmentResponseDTO department;
}
