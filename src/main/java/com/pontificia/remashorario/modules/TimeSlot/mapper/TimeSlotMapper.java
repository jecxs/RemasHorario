package com.pontificia.remashorario.modules.TimeSlot.mapper;


import com.pontificia.remashorario.modules.TimeSlot.TimeSlotEntity;
import com.pontificia.remashorario.modules.TimeSlot.dto.TimeSlotRequestDTO;
import com.pontificia.remashorario.modules.TimeSlot.dto.TimeSlotResponseDTO;

import com.pontificia.remashorario.modules.teachingHour.mapper.TeachingHourMapper;

import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component // O usa @Mapper de MapStruct
public class TimeSlotMapper {

    private final TeachingHourMapper teachingHourMapper;

    public TimeSlotMapper(TeachingHourMapper teachingHourMapper) {
        this.teachingHourMapper = teachingHourMapper;
    }

    public TimeSlotEntity toTimeSlotEntity(TimeSlotRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        TimeSlotEntity entity = new TimeSlotEntity();
        entity.setName(dto.getName());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        // Las teachingHours se generan en el servicio
        return entity;
    }

    public TimeSlotResponseDTO toTimeSlotResponseDTO(TimeSlotEntity entity) {
        if (entity == null) {
            return null;
        }
        return TimeSlotResponseDTO.builder()
                .uuid(entity.getUuid())
                .name(entity.getName())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .teachingHours(entity.getTeachingHours().stream()
                        .map(teachingHourMapper::toTeachingHourResponseDTO)
                        .collect(Collectors.toList()))
                .build();
    }
}