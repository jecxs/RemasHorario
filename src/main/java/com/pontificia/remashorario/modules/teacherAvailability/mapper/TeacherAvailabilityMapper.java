package com.pontificia.remashorario.modules.teacherAvailability.mapper;

import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teacherAvailability.TeacherAvailabilityEntity;
import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherAvailabilityRequestDTO;
import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherAvailabilityResponseDTO;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TeacherAvailabilityMapper {

    public TeacherAvailabilityResponseDTO toResponseDTO(TeacherAvailabilityEntity entity) {
        if (entity == null) return null;

        return TeacherAvailabilityResponseDTO.builder()
                .uuid(entity.getUuid())
                .dayOfWeek(entity.getDayOfWeek().name())
                .startTime(entity.getStartTime().toString())
                .endTime(entity.getEndTime().toString())
                .isAvailable(Boolean.TRUE.equals(entity.getIsAvailable()))
                .notes(entity.getNotes())
                .build();
    }

    public List<TeacherAvailabilityResponseDTO> toResponseDTOList(List<TeacherAvailabilityEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public TeacherAvailabilityEntity toEntity(TeacherAvailabilityRequestDTO dto, TeacherEntity teacher) {
        if (dto == null) return null;

        TeacherAvailabilityEntity entity = new TeacherAvailabilityEntity();
        entity.setDayOfWeek(dto.getDayOfWeek());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setIsAvailable(dto.getIsAvailable());
        entity.setTeacher(teacher);

        return entity;
    }

    public void updateEntityFromDTO(TeacherAvailabilityEntity entity, TeacherAvailabilityRequestDTO dto) {
        if (entity == null || dto == null) return;

        entity.setDayOfWeek(dto.getDayOfWeek());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
    }
}

