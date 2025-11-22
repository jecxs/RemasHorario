package com.pontificia.remashorario.modules.payrollLine;

import com.pontificia.remashorario.modules.payrollPeriod.PayrollPeriodEntity;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_line")
@Getter
@Setter
public class PayrollLineEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_period_id", nullable = false)
    private PayrollPeriodEntity payrollPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherEntity teacher;

    @Column(name = "total_hours_worked", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalHoursWorked = BigDecimal.ZERO;

    @Column(name = "total_hours_scheduled", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalHoursScheduled = BigDecimal.ZERO;

    @Column(name = "gross_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal grossAmount = BigDecimal.ZERO;

    @Column(name = "total_penalties", precision = 14, scale = 2, nullable = false)
    private BigDecimal totalPenalties = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String details;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
}
