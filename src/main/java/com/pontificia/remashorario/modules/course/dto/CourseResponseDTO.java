package com.pontificia.remashorario.modules.course.dto;


import com.pontificia.remashorario.modules.academicDepartment.dto.AcademicDepartmentResponseDTO;
import com.pontificia.remashorario.modules.career.dto.CareerResponseDTO;
import com.pontificia.remashorario.modules.cycle.dto.CycleResponseDTO;
import com.pontificia.remashorario.modules.educationalModality.dto.EducationalModalityResponseDTO;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.modules.teachingType.dto.TeachingTypeResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CourseResponseDTO {
    private UUID uuid;
    private String name;
    private Integer weeklyHours;
    private List<TeachingTypeResponseDTO> teachingTypes;
    private AcademicDepartmentResponseDTO department;
    private CycleResponseDTO cycle;
    private CareerResponseDTO career;
    private EducationalModalityResponseDTO modality;
}
