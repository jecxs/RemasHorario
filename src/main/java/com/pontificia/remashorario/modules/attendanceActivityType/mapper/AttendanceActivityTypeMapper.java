package com.pontificia.remashorario.modules.attendanceActivityType.mapper;

import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeEntity;
import com.pontificia.remashorario.modules.attendanceActivityType.dto.AttendanceActivityTypeRequestDTO;
import com.pontificia.remashorario.modules.attendanceActivityType.dto.AttendanceActivityTypeResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AttendanceActivityTypeMapper {

    public AttendanceActivityTypeResponseDTO toResponseDTO(AttendanceActivityTypeEntity entity) {
        if (entity == null) return null;
        return AttendanceActivityTypeResponseDTO.builder()
                .uuid(entity.getUuid())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    public List<AttendanceActivityTypeResponseDTO> toResponseDTOList(List<AttendanceActivityTypeEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public AttendanceActivityTypeEntity toEntity(AttendanceActivityTypeRequestDTO dto) {
        if (dto == null) return null;
        AttendanceActivityTypeEntity entity = new AttendanceActivityTypeEntity();
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        return entity;
    }

    public void updateEntityFromDTO(AttendanceActivityTypeEntity entity, AttendanceActivityTypeRequestDTO dto) {
        if (entity == null || dto == null) return;
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
    }
}
