package com.pontificia.remashorario.modules.teacher;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.teacher.dto.UserProfileDTO;
import com.pontificia.remashorario.modules.teacherAvailability.TeacherAvailabilityService;
import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherAvailabilityRequestDTO;
import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherAvailabilityResponseDTO;
import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherWithAvailabilitiesDTO;
import com.pontificia.remashorario.modules.user.UserEntity;
import com.pontificia.remashorario.modules.user.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/protected/me")
@CrossOrigin(origins = "http://localhost:4200")
public class MeController {

    private final UserService userService;
    private final TeacherService teacherService;
    private final TeacherAvailabilityService teacherAvailabilityService;

    public MeController(UserService userService, TeacherService teacherService, TeacherAvailabilityService teacherAvailabilityService) {
        this.userService = userService;
        this.teacherService = teacherService;
        this.teacherAvailabilityService = teacherAvailabilityService;
    }

    /**
     * Obtiene el perfil completo del usuario autenticado actual
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getCurrentUserProfile(Authentication authentication) {
        String email = authentication.getName();

        // Obtener el usuario actual
        UserEntity user = userService.findByEmailOrThrow(email);

        UserProfileDTO profile = UserProfileDTO.builder()
                .uuid(user.getUuid())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .active(user.getActive())
                .firstLogin(user.getFirstLogin())
                .build();

        // Si es un docente, agregar información adicional
        if (user.getRole() == UserEntity.UserRole.TEACHER && user.getTeacher() != null) {
            TeacherWithAvailabilitiesDTO teacherInfo = teacherService.getTeacherWithAvailabilities(user.getTeacher().getUuid());
            profile.setTeacher(teacherInfo);
        }

        return ResponseEntity.ok(
                ApiResponse.success(profile, "Perfil del usuario obtenido con éxito")
        );
    }

    /**
     * Obtiene solo la información de teacher del usuario autenticado (si es docente)
     */
    @GetMapping("/teacher")
    public ResponseEntity<ApiResponse<TeacherWithAvailabilitiesDTO>> getCurrentTeacherInfo(Authentication authentication) {
        String email = authentication.getName();

        UserEntity user = userService.findByEmailOrThrow(email);

        if (user.getRole() != UserEntity.UserRole.TEACHER || user.getTeacher() == null) {
            throw new IllegalStateException("El usuario actual no es un docente");
        }

        TeacherWithAvailabilitiesDTO teacherInfo = teacherService.getTeacherWithAvailabilities(user.getTeacher().getUuid());

        return ResponseEntity.ok(
                ApiResponse.success(teacherInfo, "Información del docente obtenida con éxito")
        );
    }

    /**
     * Obtiene las disponibilidades del docente autenticado actual
     */
    @GetMapping("/availabilities")
    public ResponseEntity<ApiResponse<List<TeacherAvailabilityResponseDTO>>> getCurrentTeacherAvailabilities(
            Authentication authentication) {

        String email = authentication.getName();
        UserEntity user = userService.findByEmailOrThrow(email);

        if (user.getRole() != UserEntity.UserRole.TEACHER || user.getTeacher() == null) {
            throw new IllegalStateException("El usuario actual no es un docente");
        }

        List<TeacherAvailabilityResponseDTO> availabilities =
                teacherAvailabilityService.getTeacherAvailabilities(user.getTeacher().getUuid());

        return ResponseEntity.ok(
                ApiResponse.success(availabilities, "Disponibilidades del docente obtenidas con éxito")
        );
    }

    /**
     * Crea una nueva disponibilidad para el docente autenticado
     */
    @PostMapping("/availabilities")
    public ResponseEntity<ApiResponse<TeacherAvailabilityResponseDTO>> createCurrentTeacherAvailability(
            Authentication authentication,
            @Valid @RequestBody TeacherAvailabilityRequestDTO dto) {

        String email = authentication.getName();
        UserEntity user = userService.findByEmailOrThrow(email);

        if (user.getRole() != UserEntity.UserRole.TEACHER || user.getTeacher() == null) {
            throw new IllegalStateException("El usuario actual no es un docente");
        }

        TeacherAvailabilityResponseDTO newAvailability =
                teacherAvailabilityService.createAvailability(user.getTeacher().getUuid(), dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(newAvailability, "Disponibilidad creada con éxito"));
    }

    /**
     * Elimina todas las disponibilidades del docente autenticado
     */
    @DeleteMapping("/availabilities")
    public ResponseEntity<ApiResponse<Void>> deleteAllCurrentTeacherAvailabilities(Authentication authentication) {
        String email = authentication.getName();
        UserEntity user = userService.findByEmailOrThrow(email);

        if (user.getRole() != UserEntity.UserRole.TEACHER || user.getTeacher() == null) {
            throw new IllegalStateException("El usuario actual no es un docente");
        }

        teacherAvailabilityService.deleteAllTeacherAvailabilities(user.getTeacher().getUuid());

        return ResponseEntity.ok(
                ApiResponse.success(null, "Todas las disponibilidades han sido eliminadas")
        );
    }
}