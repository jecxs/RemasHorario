package com.pontificia.remashorario.modules.modalityRate.mapper;

import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeEntity;
import com.pontificia.remashorario.modules.attendanceActivityType.mapper.AttendanceActivityTypeMapper;
import com.pontificia.remashorario.modules.educationalModality.EducationalModalityEntity;
import com.pontificia.remashorario.modules.modalityRate.ModalityRateEntity;
import com.pontificia.remashorario.modules.modalityRate.dto.ModalityRateRequestDTO;
import com.pontificia.remashorario.modules.modalityRate.dto.ModalityRateResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ModalityRateMapper {

    private final AttendanceActivityTypeMapper activityTypeMapper;

    public ModalityRateResponseDTO toResponseDTO(ModalityRateEntity entity) {
        if (entity == null) return null;

        boolean isActive = isRateActive(entity);

        return ModalityRateResponseDTO.builder()
                .uuid(entity.getUuid())
                .modality(toModalityDTO(entity.getModality()))
                .activityType(activityTypeMapper.toResponseDTO(entity.getActivityType()))
                .ratePerHour(entity.getRatePerHour())
                .effectiveFrom(entity.getEffectiveFrom())
                .effectiveTo(entity.getEffectiveTo())
                .isActive(isActive)
                .build();
    }

    public List<ModalityRateResponseDTO> toResponseDTOList(List<ModalityRateEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ModalityRateEntity toEntity(ModalityRateRequestDTO dto, EducationalModalityEntity modality,
                                       AttendanceActivityTypeEntity activityType) {
        if (dto == null) return null;
        ModalityRateEntity entity = new ModalityRateEntity();
        entity.setModality(modality);
        entity.setActivityType(activityType);
        entity.setRatePerHour(dto.getRatePerHour());
        entity.setEffectiveFrom(dto.getEffectiveFrom());
        entity.setEffectiveTo(dto.getEffectiveTo());
        return entity;
    }

    public void updateEntityFromDTO(ModalityRateEntity entity, ModalityRateRequestDTO dto) {
        if (entity == null || dto == null) return;
        entity.setRatePerHour(dto.getRatePerHour());
        entity.setEffectiveFrom(dto.getEffectiveFrom());
        entity.setEffectiveTo(dto.getEffectiveTo());
    }

    private boolean isRateActive(ModalityRateEntity entity) {
        LocalDate today = LocalDate.now();
        return (entity.getEffectiveFrom().isBefore(today) || entity.getEffectiveFrom().equals(today)) &&
               (entity.getEffectiveTo() == null || entity.getEffectiveTo().isAfter(today) || entity.getEffectiveTo().equals(today));
    }

    private com.pontificia.remashorario.modules.educationalModality.dto.EducationalModalityResponseDTO toModalityDTO(EducationalModalityEntity entity) {
        if (entity == null) return null;
        return com.pontificia.remashorario.modules.educationalModality.dto.EducationalModalityResponseDTO.builder()
                .uuid(entity.getUuid())
                .name(entity.getName())
                .durationYears(entity.getDurationYears())
                .description(entity.getDescription())
                .build();
    }
}
