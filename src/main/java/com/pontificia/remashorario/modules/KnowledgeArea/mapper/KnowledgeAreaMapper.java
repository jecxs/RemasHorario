package com.pontificia.remashorario.modules.KnowledgeArea.mapper;

import com.pontificia.remashorario.modules.KnowledgeArea.KnowledgeAreaEntity;
import com.pontificia.remashorario.modules.KnowledgeArea.dto.KnowledgeAreaRequestDTO;
import com.pontificia.remashorario.modules.KnowledgeArea.dto.KnowledgeAreaResponseDTO;
import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentEntity;
import com.pontificia.remashorario.modules.academicDepartment.dto.AcademicDepartmentResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KnowledgeAreaMapper {

    public KnowledgeAreaResponseDTO toResponseDTO(KnowledgeAreaEntity entity) {
        if (entity == null) return null;

        return KnowledgeAreaResponseDTO.builder()
                .uuid(entity.getUuid())
                .name(entity.getName())
                .description(entity.getDescription())
                .department(entity.getDepartment() != null ?
                        AcademicDepartmentResponseDTO.builder()
                                .uuid(entity.getDepartment().getUuid())
                                .name(entity.getDepartment().getName())
                                .code(entity.getDepartment().getCode())
                                .build() : null)
                .build();
    }

    public List<KnowledgeAreaResponseDTO> toResponseDTOList(List<KnowledgeAreaEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public KnowledgeAreaEntity toEntity(KnowledgeAreaRequestDTO dto, AcademicDepartmentEntity department) {
        if (dto == null) return null;

        KnowledgeAreaEntity entity = new KnowledgeAreaEntity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setDepartment(department);

        return entity;
    }

    public void updateEntityFromDTO(KnowledgeAreaEntity entity, KnowledgeAreaRequestDTO dto,
                                    AcademicDepartmentEntity department) {
        if (entity == null || dto == null) return;

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setDepartment(department);
    }
}
