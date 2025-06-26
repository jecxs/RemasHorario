package com.pontificia.remashorario.modules.classSession.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ClassSessionValidationDTO {
    @NotNull
    private UUID courseUuid;
    @NotNull
    private UUID teacherUuid;
    @NotNull
    private UUID learningSpaceUuid;
    @NotNull
    private UUID studentGroupUuid;
    @NotNull
    private String dayOfWeek;
    @NotNull
    private List<UUID> teachingHourUuids;

    @NotNull
    private UUID sessionTypeUuid;
}
