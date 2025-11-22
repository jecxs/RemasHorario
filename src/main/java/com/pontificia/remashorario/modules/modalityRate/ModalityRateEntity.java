package com.pontificia.remashorario.modules.modalityRate;

import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeEntity;
import com.pontificia.remashorario.modules.educationalModality.EducationalModalityEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "modality_rate")
@Getter
@Setter
public class ModalityRateEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modality_id", nullable = false)
    private EducationalModalityEntity modality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_type_id", nullable = false)
    private AttendanceActivityTypeEntity activityType;

    @Column(name = "rate_per_hour", nullable = false, precision = 12, scale = 2)
    private BigDecimal ratePerHour;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;
}
