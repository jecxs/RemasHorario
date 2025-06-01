package com.pontificia.remashorario.modules.learningSpaceSpecialty.dto;

import com.pontificia.remashorario.modules.academicDepartment.dto.AcademicDepartmentResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class LearningSpaceSpecialtyResponseDTO {
    private UUID uuid;
    private String name;
    private String description;
    private AcademicDepartmentResponseDTO department;
}
