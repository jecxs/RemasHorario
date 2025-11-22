package com.pontificia.remashorario.modules.academicCalendarException.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class AcademicCalendarExceptionResponseDTO {
    private UUID uuid;
    private LocalDate date;
    private String code;
    private String description;
}
