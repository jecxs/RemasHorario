package com.pontificia.remashorario.modules.learningSpace.dto;

import com.pontificia.remashorario.modules.teachingType.dto.TeachingTypeResponseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class LearningSpaceResponseDTO {
    private UUID uuid;
    private String name;
    private Integer capacity;
    private TeachingTypeResponseDTO teachingType;
}
