package com.pontificia.remashorario.modules.course.mapper;

import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentEntity;
import com.pontificia.remashorario.modules.academicDepartment.dto.AcademicDepartmentResponseDTO;
import com.pontificia.remashorario.modules.career.dto.CareerResponseDTO;
import com.pontificia.remashorario.modules.course.CourseEntity;
import com.pontificia.remashorario.modules.course.dto.CourseRequestDTO;
import com.pontificia.remashorario.modules.course.dto.CourseResponseDTO;
import com.pontificia.remashorario.modules.cycle.CycleEntity;
import com.pontificia.remashorario.modules.cycle.dto.CycleResponseDTO;
import com.pontificia.remashorario.modules.educationalModality.dto.EducationalModalityResponseDTO;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.modules.teachingType.mapper.TeachingTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CourseMapper {

    private final TeachingTypeMapper teachingTypeMapper;

    @Autowired
    public CourseMapper(TeachingTypeMapper teachingTypeMapper) {
        this.teachingTypeMapper = teachingTypeMapper;
    }

    public CourseResponseDTO toResponseDTO(CourseEntity entity) {
        if (entity == null) return null;

        return CourseResponseDTO.builder()
                .uuid(entity.getUuid())
                .name(entity.getName())
                .weeklyHours(entity.getWeeklyHours())
                .teachingTypes(teachingTypeMapper.toResponseDTOList(
                        entity.getTeachingTypes().stream().toList()))
                .department(entity.getDepartment() != null ?
                        AcademicDepartmentResponseDTO.builder()
                                .uuid(entity.getDepartment().getUuid())
                                .name(entity.getDepartment().getName())
                                .code(entity.getDepartment().getCode())
                                .build() : null)
                .cycle(CycleResponseDTO.builder()
                        .uuid(entity.getCycle().getUuid())
                        .number(entity.getCycle().getNumber())
                        .build())
                .career(CareerResponseDTO.builder()
                        .uuid(entity.getCycle().getCareer().getUuid())
                        .name(entity.getCycle().getCareer().getName())
                        .build())
                .modality(EducationalModalityResponseDTO.builder()
                        .uuid(entity.getCycle().getCareer().getModality().getUuid())
                        .name(entity.getCycle().getCareer().getModality().getName())
                        .durationYears(entity.getCycle().getCareer().getModality().getDurationYears())
                        .build())
                .build();
    }

    public List<CourseResponseDTO> toResponseDTOList(List<CourseEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public CourseEntity toEntity(CourseRequestDTO dto, CycleEntity cycle,
                                 AcademicDepartmentEntity department,
                                 Set<TeachingTypeEntity> teachingTypes) {
        if (dto == null) return null;

        CourseEntity entity = new CourseEntity();
        entity.setName(dto.getName());
        entity.setWeeklyHours(dto.getWeeklyHours());
        entity.setCycle(cycle);
        entity.setDepartment(department);
        entity.setTeachingTypes(teachingTypes);

        return entity;
    }

    public void updateEntityFromDTO(CourseEntity entity, CourseRequestDTO dto,
                                    CycleEntity cycle, AcademicDepartmentEntity department,
                                    Set<TeachingTypeEntity> teachingTypes) {
        if (entity == null || dto == null) return;

        entity.setName(dto.getName());
        entity.setWeeklyHours(dto.getWeeklyHours());
        entity.setCycle(cycle);
        entity.setDepartment(department);
        entity.setTeachingTypes(teachingTypes);
    }
}