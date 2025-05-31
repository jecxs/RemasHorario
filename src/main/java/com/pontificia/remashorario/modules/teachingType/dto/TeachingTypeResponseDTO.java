package com.pontificia.remashorario.modules.teachingType.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeachingTypeResponseDTO {
    private UUID uuid;
    private String name;
}
