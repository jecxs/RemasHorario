package com.pontificia.remashorario.modules.academicDepartment.dto;

import com.pontificia.remashorario.modules.KnowledgeArea.dto.KnowledgeAreaResponseDTO;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class AcademicDepartmentResponseDTO {
    private UUID uuid;
    private String name;
    private String code;
    private String description;
    private List<KnowledgeAreaResponseDTO> knowledgeAreas;
}
