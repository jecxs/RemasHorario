package com.pontificia.remashorario.modules.teacherAttendance.dto;

import com.pontificia.remashorario.modules.attendanceActivityType.dto.AttendanceActivityTypeResponseDTO;
import com.pontificia.remashorario.modules.teacher.dto.TeacherResponseDTO;
import com.pontificia.remashorario.modules.teacherAttendance.TeacherAttendanceEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TeacherAttendanceResponseDTO {
    private UUID uuid;
    private TeacherResponseDTO teacher;
    private UUID classSessionUuid;
    private AttendanceActivityTypeResponseDTO activityType;
    private LocalDate attendanceDate;
    private LocalTime scheduledStartTime;
    private LocalTime scheduledEndTime;
    private Integer scheduledDurationMinutes;
    private LocalDateTime checkinAt;
    private LocalDateTime checkoutAt;
    private Integer actualDurationMinutes;
    private Integer lateMinutes;
    private Integer earlyDepartureMinutes;
    private TeacherAttendanceEntity.AttendanceStatus status;
    private Boolean isHoliday;
    private String adminNote;
    private Integer totalPenaltyMinutes;
    private Boolean hasCheckIn;
    private Boolean hasCheckOut;
}
