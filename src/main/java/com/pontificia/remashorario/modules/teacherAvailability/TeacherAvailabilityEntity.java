package com.pontificia.remashorario.modules.teacherAvailability;

import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "teacher_availability",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"teacher_id", "day_of_week", "start_time"})})
@Getter
@Setter
public class TeacherAvailabilityEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherEntity teacher;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false, columnDefinition = "TIME")
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false, columnDefinition = "TIME")
    private LocalTime endTime;

    @Column(name = "is_available")
    private Boolean isAvailable;

    @Column(name = "notes")
    private String notes;

}
