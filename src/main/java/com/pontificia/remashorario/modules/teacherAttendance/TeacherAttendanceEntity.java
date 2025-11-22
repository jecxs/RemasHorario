package com.pontificia.remashorario.modules.teacherAttendance;

import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeEntity;
import com.pontificia.remashorario.modules.classSession.ClassSessionEntity;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "teacher_attendance")
@Getter
@Setter
public class TeacherAttendanceEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherEntity teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_session_id")
    private ClassSessionEntity classSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_activity_type_id", nullable = false)
    private AttendanceActivityTypeEntity attendanceActivityType;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "scheduled_start_time")
    private LocalTime scheduledStartTime;

    @Column(name = "scheduled_end_time")
    private LocalTime scheduledEndTime;

    @Column(name = "scheduled_duration_minutes")
    private Integer scheduledDurationMinutes;

    @Column(name = "checkin_at")
    private LocalDateTime checkinAt;

    @Column(name = "checkout_at")
    private LocalDateTime checkoutAt;

    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;

    @Column(name = "late_minutes", nullable = false)
    private Integer lateMinutes = 0;

    @Column(name = "early_departure_minutes", nullable = false)
    private Integer earlyDepartureMinutes = 0;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status = AttendanceStatus.PENDING;

    @Column(name = "is_holiday", nullable = false)
    private Boolean isHoliday = false;

    @Lob
    @Column(name = "admin_note")
    private String adminNote;

    public enum AttendanceStatus {
        PENDING,
        APPROVED,
        OVERRIDDEN,
        REJECTED,
        HOLIDAY
    }
}
