package com.pontificia.remashorario.modules.period.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class PeriodResponseDTO {
    private UUID uuid;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}
