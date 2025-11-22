package com.pontificia.remashorario.modules.extraAssignment.dto;

import com.pontificia.remashorario.modules.attendanceActivityType.dto.AttendanceActivityTypeResponseDTO;
import com.pontificia.remashorario.modules.teacher.dto.TeacherResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ExtraAssignmentResponseDTO {
    private UUID uuid;
    private TeacherResponseDTO teacher;
    private AttendanceActivityTypeResponseDTO activityType;
    private String title;
    private LocalDate assignmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;
    private BigDecimal ratePerHour;
    private String notes;
    private BigDecimal calculatedPayment;
}
