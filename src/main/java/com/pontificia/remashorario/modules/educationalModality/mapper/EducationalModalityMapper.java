package com.pontificia.remashorario.modules.educationalModality.mapper;


import com.pontificia.remashorario.modules.educationalModality.EducationalModalityEntity;
import com.pontificia.remashorario.modules.educationalModality.dto.EducationalModalityRequestDTO;
import com.pontificia.remashorario.modules.educationalModality.dto.EducationalModalityResponseDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EducationalModalityMapper {

    EducationalModalityResponseDTO toResponseDTO(EducationalModalityEntity entity);

    List<EducationalModalityResponseDTO> toResponseDTOList(List<EducationalModalityEntity> entities);

    @Mapping(target = "uuid", ignore = true) // si tu entidad tiene id autogenerado
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EducationalModalityEntity toEntity(EducationalModalityRequestDTO requestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(EducationalModalityRequestDTO dto, @MappingTarget EducationalModalityEntity entity);
}

