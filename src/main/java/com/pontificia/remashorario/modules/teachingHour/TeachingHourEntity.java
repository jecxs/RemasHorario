package com.pontificia.remashorario.modules.teachingHour;

import com.pontificia.remashorario.modules.TimeSlot.TimeSlotEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "teaching_hour")
@Getter
@Setter
public class TeachingHourEntity extends BaseEntity {
    @Column(name = "order_in_timeslot", nullable = false)
    private int orderInTimeSlot; // Renombré para camelCase

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "duration_minutes", nullable = false) // Renombré para claridad
    private int durationMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", referencedColumnName = "uuid", nullable = false)
    private TimeSlotEntity timeSlot;
}
