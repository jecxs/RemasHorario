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
    private UUID cycleUuid; // Agregamos el UUID del ciclo
    private Integer cycleNumber; // Agregamos el número del ciclo para fácil visualización
    private UUID periodUuid;
    private String periodName;
    // Si realmente necesitas el DTO completo del ciclo, puedes usar:
    // private CycleResponseDTO cycle;
}
