package com.pontificia.remashorario.modules.payrollPeriod.mapper;

import com.pontificia.remashorario.modules.payrollPeriod.PayrollPeriodEntity;
import com.pontificia.remashorario.modules.payrollPeriod.dto.PayrollPeriodRequestDTO;
import com.pontificia.remashorario.modules.payrollPeriod.dto.PayrollPeriodResponseDTO;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PayrollPeriodMapper {

    public PayrollPeriodResponseDTO toResponseDTO(PayrollPeriodEntity entity) {
        if (entity == null) return null;

        boolean canModify = entity.getStatus() == PayrollPeriodEntity.PayrollStatus.DRAFT;
        int daysInPeriod = (int) ChronoUnit.DAYS.between(entity.getStartDate(), entity.getEndDate()) + 1;

        return PayrollPeriodResponseDTO.builder()
                .uuid(entity.getUuid())
                .name(entity.getName())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .canModify(canModify)
                .canDelete(canModify)
                .daysInPeriod(daysInPeriod)
                .build();
    }

    public List<PayrollPeriodResponseDTO> toResponseDTOList(List<PayrollPeriodEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public PayrollPeriodEntity toEntity(PayrollPeriodRequestDTO dto) {
        if (dto == null) return null;
        PayrollPeriodEntity entity = new PayrollPeriodEntity();
        entity.setName(dto.getName());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setStatus(PayrollPeriodEntity.PayrollStatus.DRAFT);
        return entity;
    }

    public void updateEntityFromDTO(PayrollPeriodEntity entity, PayrollPeriodRequestDTO dto) {
        if (entity == null || dto == null) return;
        entity.setName(dto.getName());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
    }
}
