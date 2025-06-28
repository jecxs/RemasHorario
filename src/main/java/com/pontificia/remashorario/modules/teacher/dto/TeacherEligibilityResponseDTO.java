package com.pontificia.remashorario.modules.teacher.dto;


import com.pontificia.remashorario.modules.KnowledgeArea.dto.KnowledgeAreaResponseDTO;
import com.pontificia.remashorario.modules.academicDepartment.dto.AcademicDepartmentResponseDTO;
import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherAvailabilityResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TeacherEligibilityResponseDTO {
    private UUID uuid;
    private String fullName;
    private String email;
    private AcademicDepartmentResponseDTO department;
    private List<KnowledgeAreaResponseDTO> knowledgeAreas;
    private Boolean hasUserAccount;

    private Boolean isAvailableForTimeSlot;
    private String availabilityStatus; // "AVAILABLE", "NOT_AVAILABLE", "PARTIAL", etc.
    private List<TeacherAvailabilityResponseDTO> availabilitiesForDay;
    private String recommendedTimeSlots;

    // ✅ NUEVOS CAMPOS para conflictos
    private Boolean hasScheduleConflict;
    private List<TeacherClassConflictDTO> conflictingClasses;
    private String conflictSummary;
    private String conflictType;
}