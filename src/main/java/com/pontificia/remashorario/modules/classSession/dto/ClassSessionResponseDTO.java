package com.pontificia.remashorario.modules.classSession.dto;


import com.pontificia.remashorario.modules.course.dto.CourseResponseDTO;
import com.pontificia.remashorario.modules.learningSpace.dto.LearningSpaceResponseDTO;
import com.pontificia.remashorario.modules.studentGroup.dto.StudentGroupResponseDTO;
import com.pontificia.remashorario.modules.teacher.dto.TeacherResponseDTO;
import com.pontificia.remashorario.modules.teachingHour.dto.TeachingHourResponseDTO;
import com.pontificia.remashorario.modules.teachingType.dto.TeachingTypeResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ClassSessionResponseDTO {
    private UUID uuid;
    private StudentGroupResponseDTO studentGroup;
    private CourseResponseDTO course;
    private TeacherResponseDTO teacher;
    private LearningSpaceResponseDTO learningSpace;
    private DayOfWeek dayOfWeek;
    private TeachingTypeResponseDTO sessionType;
    private List<TeachingHourResponseDTO> teachingHours;
    private String notes;
    private Integer totalHours; // Cantidad de horas pedagógicas
    private String timeSlotName; // Nombre del turno (derivado de las horas pedagógicas)
}
