package com.pontificia.remashorario.modules.automaticSchedule.dto;


import lombok.*;


import java.util.List;

@Getter
@Setter
@Builder
public class ScheduleGenerationResultDTO {
    private boolean success;
    private String message;
    private ScheduleGenerationSummaryDTO summary;
    private List<GeneratedClassSessionDTO> generatedSessions;
    private List<ScheduleConflictDTO> conflicts;
    private List<ScheduleWarningDTO> warnings;
    private ScheduleStatisticsDTO statistics;
    private Long executionTimeMs;
}
