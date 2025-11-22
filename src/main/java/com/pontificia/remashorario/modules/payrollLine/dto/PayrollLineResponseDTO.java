package com.pontificia.remashorario.modules.payrollLine.dto;

import com.pontificia.remashorario.modules.payrollPeriod.dto.PayrollPeriodResponseDTO;
import com.pontificia.remashorario.modules.teacher.dto.TeacherResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class PayrollLineResponseDTO {
    private UUID uuid;
    private PayrollPeriodResponseDTO payrollPeriod;
    private TeacherResponseDTO teacher;
    private BigDecimal totalHoursWorked;
    private BigDecimal totalHoursScheduled;
    private BigDecimal grossAmount;
    private BigDecimal totalPenalties;
    private BigDecimal netAmount;
    private String details; // JSON string with breakdown
    private LocalDateTime generatedAt;
    private BigDecimal compliancePercentage;
}
