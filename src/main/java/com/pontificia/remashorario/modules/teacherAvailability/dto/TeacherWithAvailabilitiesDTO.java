package com.pontificia.remashorario.modules.teacherAvailability.dto;

import com.pontificia.remashorario.modules.KnowledgeArea.dto.KnowledgeAreaResponseDTO;
import com.pontificia.remashorario.modules.academicDepartment.dto.AcademicDepartmentResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TeacherWithAvailabilitiesDTO {
    private UUID uuid;
    private String fullName;
    private String email;
    private String phone;
    private AcademicDepartmentResponseDTO department;
    private List<KnowledgeAreaResponseDTO> knowledgeAreas;
    private List<TeacherAvailabilityResponseDTO> availabilities;
    private Boolean hasUserAccount;
}
