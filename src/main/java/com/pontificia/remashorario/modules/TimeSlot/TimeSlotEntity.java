package com.pontificia.remashorario.modules.TimeSlot;

import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "time_slot",
        uniqueConstraints = @UniqueConstraint(columnNames = {"start_time", "end_time"}))
@Getter
@Setter
public class TimeSlotEntity extends BaseEntity {
    @Column(name = "name", nullable = false, length = 10)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @OneToMany(mappedBy = "timeSlot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TeachingHourEntity> teachingHours = new ArrayList<>();

    // Helper method to manage bidirectional relationship if needed (optional here as TeachingHour sets TimeSlot)
    public void addTeachingHour(TeachingHourEntity teachingHour) {
        teachingHours.add(teachingHour);
        teachingHour.setTimeSlot(this);
    }

    public void removeTeachingHour(TeachingHourEntity teachingHour) {
        teachingHours.remove(teachingHour);
        teachingHour.setTimeSlot(null);
    }
}
