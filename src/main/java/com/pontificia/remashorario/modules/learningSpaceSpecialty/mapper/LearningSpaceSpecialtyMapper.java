package com.pontificia.remashorario.modules.learningSpaceSpecialty.mapper;

import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentEntity;
import com.pontificia.remashorario.modules.academicDepartment.dto.AcademicDepartmentResponseDTO;
import com.pontificia.remashorario.modules.learningSpaceSpecialty.LearningSpaceSpecialtyEntity;
import com.pontificia.remashorario.modules.learningSpaceSpecialty.dto.LearningSpaceSpecialtyRequestDTO;
import com.pontificia.remashorario.modules.learningSpaceSpecialty.dto.LearningSpaceSpecialtyResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LearningSpaceSpecialtyMapper {

    public LearningSpaceSpecialtyResponseDTO toResponseDTO(LearningSpaceSpecialtyEntity entity) {
        if (entity == null) return null;

        return LearningSpaceSpecialtyResponseDTO.builder()
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

    public List<LearningSpaceSpecialtyResponseDTO> toResponseDTOList(List<LearningSpaceSpecialtyEntity> entities) {
        return entities.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    public LearningSpaceSpecialtyEntity toEntity(LearningSpaceSpecialtyRequestDTO dto, AcademicDepartmentEntity department) {
        if (dto == null) return null;

        LearningSpaceSpecialtyEntity entity = new LearningSpaceSpecialtyEntity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setDepartment(department);

        return entity;
    }

    public void updateEntityFromDTO(LearningSpaceSpecialtyEntity entity, LearningSpaceSpecialtyRequestDTO dto, AcademicDepartmentEntity department) {
        if (entity == null || dto == null) return;

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setDepartment(department);
    }
}
