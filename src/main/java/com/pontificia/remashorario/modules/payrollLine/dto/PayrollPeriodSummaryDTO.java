package com.pontificia.remashorario.modules.payrollLine.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class PayrollPeriodSummaryDTO {
    private BigDecimal totalNetAmount;
    private BigDecimal totalPenalties;
    private BigDecimal totalGrossAmount;
    private Long teacherCount;
    private BigDecimal averageNetPerTeacher;
    private BigDecimal penaltyPercentage;
}
