package com.pontificia.remashorario.modules.extraAssignment;

import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeEntity;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "extra_assignment")
@Getter
@Setter
public class ExtraAssignmentEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherEntity teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_type_id", nullable = false)
    private AttendanceActivityTypeEntity activityType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "assignment_date", nullable = false)
    private LocalDate assignmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "rate_per_hour", precision = 12, scale = 2)
    private BigDecimal ratePerHour;

    @Lob
    private String notes;
}
