package com.pontificia.remashorario.modules.defaultRate.dto;

import com.pontificia.remashorario.modules.attendanceActivityType.dto.AttendanceActivityTypeResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class DefaultRateResponseDTO {
    private UUID uuid;
    private AttendanceActivityTypeResponseDTO activityType;
    private BigDecimal ratePerHour;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isActive;
}
