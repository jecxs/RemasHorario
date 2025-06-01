package com.pontificia.remashorario.modules.course.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class CourseFilterDTO {
    private UUID modalityUuid;
    private UUID careerUuid;
    private UUID cycleUuid;
    private UUID knowledgeAreaUuid;
    private String courseName;
}

