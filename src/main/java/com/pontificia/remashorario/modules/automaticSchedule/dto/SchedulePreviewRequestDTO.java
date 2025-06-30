package com.pontificia.remashorario.modules.automaticSchedule.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchedulePreviewRequestDTO {
    @NotNull
    private UUID periodUuid;

    private UUID modalityUuid;
    private UUID careerUuid;
    private UUID cycleUuid;
    private List<UUID> groupUuids;
}
