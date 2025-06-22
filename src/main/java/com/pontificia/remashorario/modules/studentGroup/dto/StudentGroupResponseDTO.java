package com.pontificia.remashorario.modules.studentGroup.dto;

import com.pontificia.remashorario.modules.cycle.dto.CycleResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class StudentGroupResponseDTO {
    private UUID uuid;
    private String name;
    private UUID cycleUuid;
    private Integer cycleNumber;
    private UUID periodUuid;
    private String periodName;

    // ✅ AGREGAR: Información de la carrera
    private UUID careerUuid;
    private String careerName;
    private UUID modalityUuid;      // Opcional
    private String modalityName;    // Opcional
}