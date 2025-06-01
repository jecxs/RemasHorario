package com.pontificia.remashorario.modules.studentGroup;

import com.pontificia.remashorario.modules.cycle.CycleEntity;
import com.pontificia.remashorario.modules.period.PeriodEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "student_group")
@Getter
@Setter
public class StudentGroupEntity extends BaseEntity {
    @Column(name = "name", length = 10, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "cycle_id", referencedColumnName = "uuid")
    private CycleEntity cycle;

    @ManyToOne
    @JoinColumn(name = "period_id", referencedColumnName = "uuid", nullable = false)
    private PeriodEntity period;
}
