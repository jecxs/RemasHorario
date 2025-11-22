package com.pontificia.remashorario.modules.defaultRate.mapper;

import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeEntity;
import com.pontificia.remashorario.modules.attendanceActivityType.mapper.AttendanceActivityTypeMapper;
import com.pontificia.remashorario.modules.defaultRate.DefaultRateEntity;
import com.pontificia.remashorario.modules.defaultRate.dto.DefaultRateRequestDTO;
import com.pontificia.remashorario.modules.defaultRate.dto.DefaultRateResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DefaultRateMapper {

    private final AttendanceActivityTypeMapper activityTypeMapper;

    public DefaultRateResponseDTO toResponseDTO(DefaultRateEntity entity) {
        if (entity == null) return null;

        boolean isActive = isRateActive(entity);

        return DefaultRateResponseDTO.builder()
                .uuid(entity.getUuid())
                .activityType(activityTypeMapper.toResponseDTO(entity.getActivityType()))
                .ratePerHour(entity.getRatePerHour())
                .effectiveFrom(entity.getEffectiveFrom())
                .effectiveTo(entity.getEffectiveTo())
                .isActive(isActive)
                .build();
    }

    public List<DefaultRateResponseDTO> toResponseDTOList(List<DefaultRateEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public DefaultRateEntity toEntity(DefaultRateRequestDTO dto, AttendanceActivityTypeEntity activityType) {
        if (dto == null) return null;
        DefaultRateEntity entity = new DefaultRateEntity();
        entity.setActivityType(activityType);
        entity.setRatePerHour(dto.getRatePerHour());
        entity.setEffectiveFrom(dto.getEffectiveFrom());
        entity.setEffectiveTo(dto.getEffectiveTo());
        return entity;
    }

    public void updateEntityFromDTO(DefaultRateEntity entity, DefaultRateRequestDTO dto) {
        if (entity == null || dto == null) return;
        entity.setRatePerHour(dto.getRatePerHour());
        entity.setEffectiveFrom(dto.getEffectiveFrom());
        entity.setEffectiveTo(dto.getEffectiveTo());
    }

    private boolean isRateActive(DefaultRateEntity entity) {
        LocalDate today = LocalDate.now();
        return (entity.getEffectiveFrom().isBefore(today) || entity.getEffectiveFrom().equals(today)) &&
               (entity.getEffectiveTo() == null || entity.getEffectiveTo().isAfter(today) || entity.getEffectiveTo().equals(today));
    }
}
