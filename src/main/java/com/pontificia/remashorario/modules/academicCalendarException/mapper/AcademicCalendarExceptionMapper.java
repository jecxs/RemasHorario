package com.pontificia.remashorario.modules.academicCalendarException.mapper;

import com.pontificia.remashorario.modules.academicCalendarException.AcademicCalendarExceptionEntity;
import com.pontificia.remashorario.modules.academicCalendarException.dto.AcademicCalendarExceptionRequestDTO;
import com.pontificia.remashorario.modules.academicCalendarException.dto.AcademicCalendarExceptionResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AcademicCalendarExceptionMapper {

    public AcademicCalendarExceptionResponseDTO toResponseDTO(AcademicCalendarExceptionEntity entity) {
        if (entity == null) return null;
        return AcademicCalendarExceptionResponseDTO.builder()
                .uuid(entity.getUuid())
                .date(entity.getDate())
                .code(entity.getCode())
                .description(entity.getDescription())
                .build();
    }

    public List<AcademicCalendarExceptionResponseDTO> toResponseDTOList(List<AcademicCalendarExceptionEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public AcademicCalendarExceptionEntity toEntity(AcademicCalendarExceptionRequestDTO dto) {
        if (dto == null) return null;
        AcademicCalendarExceptionEntity entity = new AcademicCalendarExceptionEntity();
        entity.setDate(dto.getDate());
        entity.setCode(dto.getCode());
        entity.setDescription(dto.getDescription());
        return entity;
    }

    public void updateEntityFromDTO(AcademicCalendarExceptionEntity entity, AcademicCalendarExceptionRequestDTO dto) {
        if (entity == null || dto == null) return;
        entity.setDate(dto.getDate());
        entity.setCode(dto.getCode());
        entity.setDescription(dto.getDescription());
    }
}
