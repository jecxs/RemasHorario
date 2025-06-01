package com.pontificia.remashorario.modules.classSession;

import com.pontificia.remashorario.modules.course.CourseEntity;
import com.pontificia.remashorario.modules.learningSpace.LearningSpaceEntity;
import com.pontificia.remashorario.modules.studentGroup.StudentGroupEntity;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.modules.period.PeriodEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "class_session")
@Getter
@Setter
public class ClassSessionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_group_id", nullable = false)
    private StudentGroupEntity studentGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherEntity teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_space_id", nullable = false)
    private LearningSpaceEntity learningSpace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    private PeriodEntity period;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_type_id", nullable = false)
    private TeachingTypeEntity sessionType;

    @ManyToMany
    @JoinTable(
            name = "class_session_teaching_hour",
            joinColumns = @JoinColumn(name = "class_session_id"),
            inverseJoinColumns = @JoinColumn(name = "teaching_hour_id")
    )
    private Set<TeachingHourEntity> teachingHours = new HashSet<>();

    @Column(length = 500)
    private String notes;


    public void addTeachingHour(TeachingHourEntity teachingHour) {
        teachingHours.add(teachingHour);
    }

    public void removeTeachingHour(TeachingHourEntity teachingHour) {
        teachingHours.remove(teachingHour);
    }

    public void clearTeachingHours() {
        teachingHours.clear();
    }
}
