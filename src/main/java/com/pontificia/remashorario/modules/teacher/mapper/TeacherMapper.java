package com.pontificia.remashorario.modules.teacher.mapper;


import com.pontificia.remashorario.modules.KnowledgeArea.KnowledgeAreaEntity;
import com.pontificia.remashorario.modules.KnowledgeArea.mapper.KnowledgeAreaMapper;
import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentEntity;
import com.pontificia.remashorario.modules.academicDepartment.mapper.AcademicDepartmentMapper;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teacher.dto.TeacherRequestDTO;
import com.pontificia.remashorario.modules.teacher.dto.TeacherResponseDTO;
import com.pontificia.remashorario.modules.teacher.dto.TeacherUpdateDTO;
import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherWithAvailabilitiesDTO;
import com.pontificia.remashorario.modules.teacherAvailability.mapper.TeacherAvailabilityMapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TeacherMapper {

    private final AcademicDepartmentMapper departmentMapper;
    private final KnowledgeAreaMapper knowledgeAreaMapper;
    private final TeacherAvailabilityMapper availabilityMapper;

    @Autowired
    public TeacherMapper(AcademicDepartmentMapper departmentMapper,
                         KnowledgeAreaMapper knowledgeAreaMapper,
                         TeacherAvailabilityMapper availabilityMapper) {
        this.departmentMapper = departmentMapper;
        this.knowledgeAreaMapper = knowledgeAreaMapper;
        this.availabilityMapper = availabilityMapper;
    }

    public TeacherResponseDTO toResponseDTO(TeacherEntity entity) {
        if (entity == null) return null;

        return TeacherResponseDTO.builder()
                .uuid(entity.getUuid())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .department(departmentMapper.toResponseDTO(entity.getDepartment()))
                .knowledgeAreas(entity.getKnowledgeAreas() != null ?
                        knowledgeAreaMapper.toResponseDTOList(
                                entity.getKnowledgeAreas().stream().toList()) : new ArrayList<>())
                .hasUserAccount(entity.getHasUserAccount())
                .totalAvailabilities(entity.getAvailabilities() != null ?
                        entity.getAvailabilities().size() : 0)
                .build();
    }

    public TeacherWithAvailabilitiesDTO toWithAvailabilitiesDTO(TeacherEntity entity) {
        if (entity == null) return null;

        return TeacherWithAvailabilitiesDTO.builder()
                .uuid(entity.getUuid())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .department(departmentMapper.toResponseDTO(entity.getDepartment()))
                .knowledgeAreas(entity.getKnowledgeAreas() != null ?
                        knowledgeAreaMapper.toResponseDTOList(
                                entity.getKnowledgeAreas().stream().toList()) : new ArrayList<>())
                .availabilities(entity.getAvailabilities() != null ?
                        availabilityMapper.toResponseDTOList(entity.getAvailabilities()) : new ArrayList<>())
                .hasUserAccount(entity.getHasUserAccount())
                .build();
    }

    public List<TeacherResponseDTO> toResponseDTOList(List<TeacherEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public TeacherEntity toEntity(TeacherRequestDTO dto,
                                  AcademicDepartmentEntity department,
                                  Set<KnowledgeAreaEntity> knowledgeAreas) {
        if (dto == null) return null;

        TeacherEntity entity = new TeacherEntity();
        entity.setFullName(dto.getFullName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setDepartment(department);
        entity.setKnowledgeAreas(knowledgeAreas);
        entity.setHasUserAccount(false);

        return entity;
    }

    public void updateEntityFromDTO(TeacherEntity entity,
                                    TeacherUpdateDTO dto,
                                    AcademicDepartmentEntity department,
                                    Set<KnowledgeAreaEntity> knowledgeAreas) {
        if (entity == null || dto == null) return;

        entity.setFullName(dto.getFullName());
        entity.setPhone(dto.getPhone());
        entity.setDepartment(department);
        entity.setKnowledgeAreas(knowledgeAreas);
    }
}
