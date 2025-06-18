package com.pontificia.remashorario.modules.teachingHour.mapper;

import com.pontificia.remashorario.modules.TimeSlot.TimeSlotEntity;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import com.pontificia.remashorario.modules.teachingHour.dto.TeachingHourRequestDTO;
import com.pontificia.remashorario.modules.teachingHour.dto.TeachingHourResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component // O usa @Mapper de MapStruct
public class TeachingHourMapper {

    public TeachingHourResponseDTO toTeachingHourResponseDTO(TeachingHourEntity entity) {
        if (entity == null) {
            return null;
        }
        return TeachingHourResponseDTO.builder()
                .uuid(entity.getUuid())
                .orderInTimeSlot(entity.getOrderInTimeSlot())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .durationMinutes(entity.getDurationMinutes())
                .build();
    }

    public List<TeachingHourResponseDTO> toResponseDTOList(List<TeachingHourEntity> entities) {
        return entities.stream()
                .map(this::toTeachingHourResponseDTO)
                .collect(Collectors.toList());
    }
}
