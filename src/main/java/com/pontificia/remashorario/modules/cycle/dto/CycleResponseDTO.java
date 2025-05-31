package com.pontificia.remashorario.modules.cycle.dto;

import com.pontificia.remashorario.modules.career.dto.CareerResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class CycleResponseDTO {
    private UUID uuid;
    private Integer number;
    private CareerResponseDTO career;
}

