package com.pontificia.remashorario.modules.payrollLine.mapper;

import com.pontificia.remashorario.modules.payrollLine.PayrollLineEntity;
import com.pontificia.remashorario.modules.payrollLine.dto.PayrollLineResponseDTO;
import com.pontificia.remashorario.modules.payrollLine.dto.PayrollPeriodSummaryDTO;
import com.pontificia.remashorario.modules.payrollPeriod.mapper.PayrollPeriodMapper;
import com.pontificia.remashorario.modules.teacher.mapper.TeacherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PayrollLineMapper {

    private final PayrollPeriodMapper payrollPeriodMapper;
    private final TeacherMapper teacherMapper;

    public PayrollLineResponseDTO toResponseDTO(PayrollLineEntity entity) {
        if (entity == null) return null;

        BigDecimal compliancePercentage = BigDecimal.ZERO;
        if (entity.getTotalHoursScheduled().compareTo(BigDecimal.ZERO) > 0) {
            compliancePercentage = entity.getTotalHoursWorked()
                    .divide(entity.getTotalHoursScheduled(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return PayrollLineResponseDTO.builder()
                .uuid(entity.getUuid())
                .payrollPeriod(payrollPeriodMapper.toResponseDTO(entity.getPayrollPeriod()))
                .teacher(teacherMapper.toResponseDTO(entity.getTeacher()))
                .totalHoursWorked(entity.getTotalHoursWorked())
                .totalHoursScheduled(entity.getTotalHoursScheduled())
                .grossAmount(entity.getGrossAmount())
                .totalPenalties(entity.getTotalPenalties())
                .netAmount(entity.getNetAmount())
                .details(entity.getDetails())
                .generatedAt(entity.getGeneratedAt())
                .compliancePercentage(compliancePercentage)
                .build();
    }

    public List<PayrollLineResponseDTO> toResponseDTOList(List<PayrollLineEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public PayrollPeriodSummaryDTO toSummaryDTO(BigDecimal totalNet, BigDecimal totalPenalties,
                                               BigDecimal totalGross, Long teacherCount) {
        BigDecimal averageNetPerTeacher = BigDecimal.ZERO;
        if (teacherCount > 0) {
            averageNetPerTeacher = totalNet.divide(BigDecimal.valueOf(teacherCount), 2, RoundingMode.HALF_UP);
        }

        BigDecimal penaltyPercentage = BigDecimal.ZERO;
        if (totalGross.compareTo(BigDecimal.ZERO) > 0) {
            penaltyPercentage = totalPenalties.divide(totalGross, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return PayrollPeriodSummaryDTO.builder()
                .totalNetAmount(totalNet)
                .totalPenalties(totalPenalties)
                .totalGrossAmount(totalGross)
                .teacherCount(teacherCount)
                .averageNetPerTeacher(averageNetPerTeacher)
                .penaltyPercentage(penaltyPercentage)
                .build();
    }
}
