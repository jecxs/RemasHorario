package com.pontificia.remashorario.modules.teachingType.mapper;

import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.modules.teachingType.dto.TeachingTypeResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TeachingTypeMapper {
    /**
     * Convierte una entidad TeachingTypeEntity a su correspondiente DTO TeachingTypeResponseDTO.
     *
     * @param entity La entidad TeachingTypeEntity que se quiere convertir.
     * @return El DTO TeachingTypeResponseDTO correspondiente a la entidad.
     */
    public TeachingTypeResponseDTO toResponseDTO(TeachingTypeEntity entity) {
        if (entity == null) return null;

        return TeachingTypeResponseDTO.builder()
                .uuid(entity.getUuid())
                .name(entity.getName().name())
                .build();
    }

    /**
     * Convierte una lista de entidades TeachingTypeEntity a una lista de DTO TeachingTypeResponseDTO.
     *
     * @param entities La lista de entidades TeachingTypeEntity que se quieren convertir.
     * @return La lista de DTO TeachingTypeResponseDTO correspondiente a las entidades.
     */
    public List<TeachingTypeResponseDTO> toResponseDTOList(List<TeachingTypeEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}
