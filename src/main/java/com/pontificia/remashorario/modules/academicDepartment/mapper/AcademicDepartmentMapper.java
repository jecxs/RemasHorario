package com.pontificia.remashorario.modules.academicDepartment.mapper;

import com.pontificia.remashorario.modules.KnowledgeArea.mapper.KnowledgeAreaMapper;
import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentEntity;
import com.pontificia.remashorario.modules.academicDepartment.dto.AcademicDepartmentRequestDTO;
import com.pontificia.remashorario.modules.academicDepartment.dto.AcademicDepartmentResponseDTO;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AcademicDepartmentMapper {

    private final KnowledgeAreaMapper knowledgeAreaMapper;

    @Autowired
    public AcademicDepartmentMapper(KnowledgeAreaMapper knowledgeAreaMapper) {
        this.knowledgeAreaMapper = knowledgeAreaMapper;
    }

    public AcademicDepartmentResponseDTO toResponseDTO(AcademicDepartmentEntity entity) {
        if (entity == null) return null;

        return AcademicDepartmentResponseDTO.builder()
                .uuid(entity.getUuid())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .knowledgeAreas(entity.getKnowledgeAreas() != null ?
                        knowledgeAreaMapper.toResponseDTOList(
                                entity.getKnowledgeAreas().stream().toList()) : null)
                .build();
    }

    public List<AcademicDepartmentResponseDTO> toResponseDTOList(List<AcademicDepartmentEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public AcademicDepartmentEntity toEntity(AcademicDepartmentRequestDTO dto) {
        if (dto == null) return null;

        AcademicDepartmentEntity entity = new AcademicDepartmentEntity();
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setDescription(dto.getDescription());

        return entity;
    }

    public void updateEntityFromDTO(AcademicDepartmentEntity entity, AcademicDepartmentRequestDTO dto) {
        if (entity == null || dto == null) return;

        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setDescription(dto.getDescription());
    }
}
