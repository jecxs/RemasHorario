package com.pontificia.remashorario.modules.payrollPeriod;

import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "payroll_period")
@Getter
@Setter
public class PayrollPeriodEntity extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private PayrollStatus status = PayrollStatus.DRAFT;

    public enum PayrollStatus {
        DRAFT,
        CALCULATED,
        APPROVED,
        PAID
    }
}
