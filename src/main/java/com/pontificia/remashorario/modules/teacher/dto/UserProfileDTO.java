package com.pontificia.remashorario.modules.teacher.dto;

import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherWithAvailabilitiesDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class UserProfileDTO {
    private UUID uuid;
    private String email;
    private String fullName;
    private String role;
    private Boolean active;
    private Boolean firstLogin;

    // Información adicional si es docente
    private TeacherWithAvailabilitiesDTO teacher;
}