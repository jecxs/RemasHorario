package com.pontificia.remashorario.modules.teacher.dto;

import com.pontificia.remashorario.modules.KnowledgeArea.dto.KnowledgeAreaResponseDTO;
import com.pontificia.remashorario.modules.academicDepartment.dto.AcademicDepartmentResponseDTO;
import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherAvailabilityResponseDTO;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TeacherResponseDTO {
    private UUID uuid;
    private String fullName;
    private String email;
    private String phone;
    private AcademicDepartmentResponseDTO department;
    private List<KnowledgeAreaResponseDTO> knowledgeAreas;
    private Boolean hasUserAccount;
    private Integer totalAvailabilities;
}
