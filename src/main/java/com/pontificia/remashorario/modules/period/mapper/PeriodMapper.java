package com.pontificia.remashorario.modules.period.mapper;

import com.pontificia.remashorario.modules.period.PeriodEntity;
import com.pontificia.remashorario.modules.period.dto.PeriodRequestDTO;
import com.pontificia.remashorario.modules.period.dto.PeriodResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PeriodMapper {

    public PeriodResponseDTO toResponseDTO(PeriodEntity entity) {
        if (entity == null) return null;
        return PeriodResponseDTO.builder()
                .uuid(entity.getUuid())
                .name(entity.getName())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .build();
    }

    public List<PeriodResponseDTO> toResponseDTOList(List<PeriodEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public PeriodEntity toEntity(PeriodRequestDTO dto) {
        if (dto == null) return null;
        PeriodEntity entity = new PeriodEntity();
        entity.setName(dto.getName());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        return entity;
    }

    public void updateEntityFromDTO(PeriodEntity entity, PeriodRequestDTO dto) {
        if (entity == null || dto == null) return;
        entity.setName(dto.getName());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
    }
}
