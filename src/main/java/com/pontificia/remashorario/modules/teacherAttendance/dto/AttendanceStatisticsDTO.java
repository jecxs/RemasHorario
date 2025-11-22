package com.pontificia.remashorario.modules.teacherAttendance.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class AttendanceStatisticsDTO {
    private Integer totalMinutesWorked;
    private Integer totalScheduledMinutes;
    private Integer totalLateMinutes;
    private Integer totalEarlyDepartureMinutes;
    private Long approvedCount;
    private Long pendingCount;
    private BigDecimal totalHoursWorked;
    private BigDecimal totalHoursScheduled;
    private BigDecimal compliancePercentage;
}
