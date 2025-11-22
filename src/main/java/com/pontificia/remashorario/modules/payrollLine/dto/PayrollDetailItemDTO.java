package com.pontificia.remashorario.modules.payrollLine.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class PayrollDetailItemDTO {
    private String type; // "ATTENDANCE" or "EXTRA_ASSIGNMENT"
    private String date;
    private String activityType;
    private String description;
    private BigDecimal ratePerHour;
    private BigDecimal hoursWorked;
    private BigDecimal hoursScheduled;
    private BigDecimal grossAmount;
    private Integer penaltyMinutes;
    private BigDecimal penaltyAmount;
    private Integer lateMinutes;
    private Integer earlyDepartureMinutes;
}
