package com.pontificia.remashorario.modules.automaticSchedule.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleCleanupRequestDTO {
    @NotNull
    private UUID periodUuid;

    @NotEmpty
    private List<UUID> groupUuids;

    @NotNull
    private CleanupStrategy strategy;

    private Boolean confirmOverwrite = false;

    public enum CleanupStrategy {
        RESET_ALL,          // Eliminar todas las sesiones existentes
        SELECTIVE_CLEANUP,  // Eliminar solo sesiones problemáticas
        COMPLETE_EXISTING   // Mantener existentes y completar faltantes
    }
}
