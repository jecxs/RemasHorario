package com.pontificia.remashorario.modules.TimeSlot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class TimeSlotUpdateDTO {

    @NotNull(message = "UUID is required")
    private UUID uuid;

    @NotBlank(message = "Name is required")
    @Size(max = 10, message = "Name must not exceed 10 characters")
    private String name;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;
}
