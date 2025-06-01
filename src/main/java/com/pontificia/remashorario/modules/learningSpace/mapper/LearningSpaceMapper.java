package com.pontificia.remashorario.modules.learningSpace.mapper;

import com.pontificia.remashorario.modules.learningSpace.LearningSpaceEntity;
import com.pontificia.remashorario.modules.learningSpace.dto.LearningSpaceRequestDTO;
import com.pontificia.remashorario.modules.learningSpace.dto.LearningSpaceResponseDTO;
import com.pontificia.remashorario.modules.learningSpaceSpecialty.LearningSpaceSpecialtyEntity;
import com.pontificia.remashorario.modules.learningSpaceSpecialty.LearningSpaceSpecialtyService;
import com.pontificia.remashorario.modules.learningSpaceSpecialty.dto.LearningSpaceSpecialtyResponseDTO;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeService;
import com.pontificia.remashorario.modules.teachingType.dto.TeachingTypeResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LearningSpaceMapper {

    private final TeachingTypeService teachingTypeService;
    private final LearningSpaceSpecialtyService specialtyService;

    public LearningSpaceMapper(TeachingTypeService teachingTypeService,
                               LearningSpaceSpecialtyService specialtyService) {
        this.teachingTypeService = teachingTypeService;
        this.specialtyService = specialtyService;
    }

    /**
     * Convierte una entidad a un DTO de respuesta
     * @param entity Entidad a convertir
     * @return DTO de respuesta
     */
    public LearningSpaceResponseDTO toResponseDTO(LearningSpaceEntity entity) {
        if (entity == null) return null;

        return LearningSpaceResponseDTO.builder()
                .uuid(entity.getUuid())
                .name(entity.getName())
                .capacity(entity.getCapacity())
                .teachingType(TeachingTypeResponseDTO.builder()
                        .uuid(entity.getTypeUUID().getUuid())
                        .name(entity.getTypeUUID().getName().name())
                        .build())
                .specialty(entity.getSpecialty() != null ?
                        LearningSpaceSpecialtyResponseDTO.builder()
                                .uuid(entity.getSpecialty().getUuid())
                                .name(entity.getSpecialty().getName())
                                .description(entity.getSpecialty().getDescription())
                                .build() : null)
                .build();
    }

    /**
     * Convierte un DTO de solicitud a una entidad para crear
     * @param requestDTO DTO de solicitud
     * @return Entidad nueva
     */
    public LearningSpaceEntity toEntity(LearningSpaceRequestDTO requestDTO) {
        if (requestDTO == null) return null;

        LearningSpaceEntity entity = new LearningSpaceEntity();
        entity.setName(requestDTO.getName());
        entity.setCapacity(requestDTO.getCapacity());

        TeachingTypeEntity type = teachingTypeService.findOrThrow(requestDTO.getTypeUUID());
        entity.setTypeUUID(type);

        if (requestDTO.getSpecialtyUuid() != null) {
            LearningSpaceSpecialtyEntity specialty = specialtyService.findSpecialtyOrThrow(requestDTO.getSpecialtyUuid());
            entity.setSpecialty(specialty);
        } else {
            entity.setSpecialty(null);
        }

        return entity;
    }

    /**
     * Actualiza una entidad existente con datos del DTO de solicitud
     * @param requestDTO DTO con datos nuevos
     * @param entity Entidad a actualizar
     */
    public void updateEntityFromDTO(LearningSpaceRequestDTO requestDTO, LearningSpaceEntity entity) {
        if (requestDTO == null || entity == null) return;

        entity.setName(requestDTO.getName());
        entity.setCapacity(requestDTO.getCapacity());

        TeachingTypeEntity type = teachingTypeService.findOrThrow(requestDTO.getTypeUUID());
        entity.setTypeUUID(type);

        if (requestDTO.getSpecialtyUuid() != null) {
            LearningSpaceSpecialtyEntity specialty = specialtyService.findSpecialtyOrThrow(requestDTO.getSpecialtyUuid());
            entity.setSpecialty(specialty);
        } else {
            entity.setSpecialty(null);
        }
    }

    /**
     * Convierte una lista de entidades a DTO de respuesta
     * @param entities Lista de entidades
     * @return Lista de DTOs
     */
    public List<LearningSpaceResponseDTO> toResponseDTOList(List<LearningSpaceEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}
