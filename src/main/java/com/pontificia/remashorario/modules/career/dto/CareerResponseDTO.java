package com.pontificia.remashorario.modules.career.dto;


import com.pontificia.remashorario.modules.cycle.dto.CycleResponseDTO;
import com.pontificia.remashorario.modules.educationalModality.dto.EducationalModalityResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CareerResponseDTO {
    private UUID uuid;
    private String name;
    private EducationalModalityResponseDTO modality;
    private List<CycleResponseDTO> cycles;
}

