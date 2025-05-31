package com.pontificia.remashorario.modules.teacher.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TeacherFilterDTO {
    private UUID departmentUuid;
    private List<UUID> knowledgeAreaUuids;
    private String searchTerm; // para buscar por nombre o email
    private Boolean hasUserAccount;
}
