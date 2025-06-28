package com.pontificia.remashorario.modules.teacher.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class TeacherClassConflictDTO {
    private String sessionUuid;
    private String courseName;
    private String courseCode;
    private String studentGroupName;
    private String studentGroupUuid;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String learningSpaceName;
    private String sessionType;
    private List<String> conflictingHourUuids;
}

