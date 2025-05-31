package com.pontificia.remashorario.modules.educationalModality.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EducationalModalityResponseDTO {
    private UUID uuid;
    private String name;
    private Integer durationYears;
    private String description;
    private Date createdAt;
    private Date updatedAt;
}
