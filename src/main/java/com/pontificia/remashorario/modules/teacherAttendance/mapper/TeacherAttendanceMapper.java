package com.pontificia.remashorario.modules.teacherAttendance.mapper;

import com.pontificia.remashorario.modules.attendanceActivityType.mapper.AttendanceActivityTypeMapper;
import com.pontificia.remashorario.modules.teacher.mapper.TeacherMapper;
import com.pontificia.remashorario.modules.teacherAttendance.TeacherAttendanceEntity;
import com.pontificia.remashorario.modules.teacherAttendance.TeacherAttendanceService;
import com.pontificia.remashorario.modules.teacherAttendance.dto.AttendanceStatisticsDTO;
import com.pontificia.remashorario.modules.teacherAttendance.dto.TeacherAttendanceResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeacherAttendanceMapper {

    private final AttendanceActivityTypeMapper activityTypeMapper;
    private final TeacherMapper teacherMapper;

    public TeacherAttendanceResponseDTO toResponseDTO(TeacherAttendanceEntity entity) {
        if (entity == null) return null;

        int totalPenaltyMinutes = entity.getLateMinutes() + entity.getEarlyDepartureMinutes();

        return TeacherAttendanceResponseDTO.builder()
                .uuid(entity.getUuid())
                .teacher(teacherMapper.toResponseDTO(entity.getTeacher()))
                .classSessionUuid(entity.getClassSession() != null ? entity.getClassSession().getUuid() : null)
                .activityType(activityTypeMapper.toResponseDTO(entity.getAttendanceActivityType()))
                .attendanceDate(entity.getAttendanceDate())
                .scheduledStartTime(entity.getScheduledStartTime())
                .scheduledEndTime(entity.getScheduledEndTime())
                .scheduledDurationMinutes(entity.getScheduledDurationMinutes())
                .checkinAt(entity.getCheckinAt())
                .checkoutAt(entity.getCheckoutAt())
                .actualDurationMinutes(entity.getActualDurationMinutes())
                .lateMinutes(entity.getLateMinutes())
                .earlyDepartureMinutes(entity.getEarlyDepartureMinutes())
                .status(entity.getStatus())
                .isHoliday(entity.getIsHoliday())
                .adminNote(entity.getAdminNote())
                .totalPenaltyMinutes(totalPenaltyMinutes)
                .hasCheckIn(entity.getCheckinAt() != null)
                .hasCheckOut(entity.getCheckoutAt() != null)
                .build();
    }

    public List<TeacherAttendanceResponseDTO> toResponseDTOList(List<TeacherAttendanceEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public AttendanceStatisticsDTO toStatisticsDTO(TeacherAttendanceService.AttendanceStats stats) {
        if (stats == null) return null;

        BigDecimal totalHoursWorked = BigDecimal.valueOf(stats.totalMinutesWorked)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        BigDecimal totalHoursScheduled = BigDecimal.valueOf(stats.totalScheduledMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        BigDecimal compliancePercentage = BigDecimal.ZERO;
        if (stats.totalScheduledMinutes > 0) {
            compliancePercentage = BigDecimal.valueOf(stats.totalMinutesWorked)
                    .divide(BigDecimal.valueOf(stats.totalScheduledMinutes), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return AttendanceStatisticsDTO.builder()
                .totalMinutesWorked(stats.totalMinutesWorked)
                .totalScheduledMinutes(stats.totalScheduledMinutes)
                .totalLateMinutes(stats.totalLateMinutes)
                .totalEarlyDepartureMinutes(stats.totalEarlyDepartureMinutes)
                .approvedCount(stats.approvedCount)
                .pendingCount(stats.pendingCount)
                .totalHoursWorked(totalHoursWorked)
                .totalHoursScheduled(totalHoursScheduled)
                .compliancePercentage(compliancePercentage)
                .build();
    }
}
