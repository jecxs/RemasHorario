package com.pontificia.remashorario.modules.payrollPeriod.dto;

import com.pontificia.remashorario.modules.payrollPeriod.PayrollPeriodEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class PayrollPeriodResponseDTO {
    private UUID uuid;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private PayrollPeriodEntity.PayrollStatus status;
    private Boolean canModify;
    private Boolean canDelete;
    private Integer daysInPeriod;
}
