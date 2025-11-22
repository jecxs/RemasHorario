package com.pontificia.remashorario.modules.teacherRate.dto;

import com.pontificia.remashorario.modules.attendanceActivityType.dto.AttendanceActivityTypeResponseDTO;
import com.pontificia.remashorario.modules.teacher.dto.TeacherResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TeacherRateResponseDTO {
    private UUID uuid;
    private TeacherResponseDTO teacher;
    private AttendanceActivityTypeResponseDTO activityType;
    private BigDecimal ratePerHour;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isActive;
}
