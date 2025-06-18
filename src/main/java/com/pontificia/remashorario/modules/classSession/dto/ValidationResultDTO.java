package com.pontificia.remashorario.modules.classSession.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ValidationResultDTO {
    private boolean isValid;
    private List<String> errors;
    private List<String> warnings;
    private List<String> suggestions;
    private String conflictType; // TEACHER, SPACE, GROUP, MULTIPLE
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL


}