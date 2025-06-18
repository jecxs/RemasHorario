package com.pontificia.remashorario.modules.classSession.dto;


import com.pontificia.remashorario.modules.learningSpace.dto.LearningSpaceResponseDTO;
import com.pontificia.remashorario.modules.teacher.dto.TeacherResponseDTO;
import com.pontificia.remashorario.modules.teachingHour.dto.TeachingHourResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class IntelliSenseDTO {
    private List<TeacherResponseDTO> eligibleTeachers;
    private List<LearningSpaceResponseDTO> eligibleSpaces;
    private List<TeachingHourResponseDTO> availableHours;
    private List<String> recommendations;
    private List<String> warnings;

    // constructors, getters, setters
}
