package com.pontificia.remashorario.modules.modalityRate.dto;

import com.pontificia.remashorario.modules.attendanceActivityType.dto.AttendanceActivityTypeResponseDTO;
import com.pontificia.remashorario.modules.educationalModality.dto.EducationalModalityResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ModalityRateResponseDTO {
    private UUID uuid;
    private EducationalModalityResponseDTO modality;
    private AttendanceActivityTypeResponseDTO activityType;
    private BigDecimal ratePerHour;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isActive;
}
