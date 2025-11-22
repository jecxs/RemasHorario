package com.pontificia.remashorario.modules.attendanceActivityType.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class AttendanceActivityTypeResponseDTO {
    private UUID uuid;
    private String code;
    private String name;
    private String description;
}
