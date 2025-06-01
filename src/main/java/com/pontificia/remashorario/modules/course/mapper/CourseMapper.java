package com.pontificia.remashorario.modules.course.mapper;

import com.pontificia.remashorario.modules.learningSpaceSpecialty.LearningSpaceSpecialtyEntity;
import com.pontificia.remashorario.modules.learningSpaceSpecialty.dto.LearningSpaceSpecialtyResponseDTO;
import com.pontificia.remashorario.modules.KnowledgeArea.KnowledgeAreaEntity;
import com.pontificia.remashorario.modules.KnowledgeArea.dto.KnowledgeAreaResponseDTO;
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
                .code(entity.getCode())
                .weeklyTheoryHours(entity.getWeeklyTheoryHours())
                .weeklyPracticeHours(entity.getWeeklyPracticeHours())
                .teachingTypes(teachingTypeMapper.toResponseDTOList(
                        entity.getTeachingTypes().stream().toList()))
                .teachingKnowledgeArea(entity.getTeachingKnowledgeArea() != null ?
                        KnowledgeAreaResponseDTO.builder()
                                .uuid(entity.getTeachingKnowledgeArea().getUuid())
                                .name(entity.getTeachingKnowledgeArea().getName())
                                .description(entity.getTeachingKnowledgeArea().getDescription())
                                .build() : null)
                .preferredSpecialty(entity.getPreferredSpecialty() != null ?
                        LearningSpaceSpecialtyResponseDTO.builder()
                                .uuid(entity.getPreferredSpecialty().getUuid())
                                .name(entity.getPreferredSpecialty().getName())
                                .description(entity.getPreferredSpecialty().getDescription())
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
                                 KnowledgeAreaEntity knowledgeArea,
                                 LearningSpaceSpecialtyEntity specialty,
                                 Set<TeachingTypeEntity> teachingTypes) {
        if (dto == null) return null;

        CourseEntity entity = new CourseEntity();
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setWeeklyTheoryHours(dto.getWeeklyTheoryHours());
        entity.setWeeklyPracticeHours(dto.getWeeklyPracticeHours());
        entity.setCycle(cycle);
        entity.setTeachingKnowledgeArea(knowledgeArea);
        entity.setPreferredSpecialty(specialty);
        entity.setTeachingTypes(teachingTypes);

        return entity;
    }

    public void updateEntityFromDTO(CourseEntity entity, CourseRequestDTO dto,
                                    CycleEntity cycle, KnowledgeAreaEntity knowledgeArea,
                                    LearningSpaceSpecialtyEntity specialty,
                                    Set<TeachingTypeEntity> teachingTypes) {
        if (entity == null || dto == null) return;

        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setWeeklyTheoryHours(dto.getWeeklyTheoryHours());
        entity.setWeeklyPracticeHours(dto.getWeeklyPracticeHours());
        entity.setCycle(cycle);
        entity.setTeachingKnowledgeArea(knowledgeArea);
        entity.setPreferredSpecialty(specialty);
        entity.setTeachingTypes(teachingTypes);
    }
}