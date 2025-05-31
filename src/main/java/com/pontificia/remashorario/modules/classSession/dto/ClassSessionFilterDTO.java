package com.pontificia.remashorario.modules.classSession.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ClassSessionFilterDTO {
    private UUID studentGroupUuid;
    private UUID courseUuid;
    private UUID teacherUuid;
    private UUID learningSpaceUuid;
    private DayOfWeek dayOfWeek;
    private UUID cycleUuid;
    private UUID careerUuid;
    private UUID sessionTypeUuid;
}

