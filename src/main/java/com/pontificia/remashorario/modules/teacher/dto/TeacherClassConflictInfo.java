package com.pontificia.remashorario.modules.teacher.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class TeacherClassConflictInfo {
    private boolean hasConflict;
    private List<TeacherClassConflictDTO> conflicts;
    private String conflictType; // "NONE", "FULL_CONFLICT", "PARTIAL_CONFLICT", "ERROR"
    private List<String> conflictingHourUuids;
}