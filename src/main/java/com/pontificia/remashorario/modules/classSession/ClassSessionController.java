package com.pontificia.remashorario.modules.classSession;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.classSession.dto.ClassSessionFilterDTO;
import com.pontificia.remashorario.modules.classSession.dto.ClassSessionRequestDTO;
import com.pontificia.remashorario.modules.classSession.dto.ClassSessionResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/protected/class-sessions")
@RequiredArgsConstructor
public class ClassSessionController {

    private final ClassSessionService classSessionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassSessionResponseDTO>>> getAllClassSessions() {
        List<ClassSessionResponseDTO> sessions = classSessionService.getAllClassSessions();
        return ResponseEntity.ok(
                ApiResponse.success(sessions, "Sesiones de clase recuperadas con éxito")
        );
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<ClassSessionResponseDTO>> getClassSessionById(@PathVariable UUID uuid) {
        ClassSessionResponseDTO session = classSessionService.getClassSessionById(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(session, "Sesión de clase recuperada con éxito")
        );
    }

    @GetMapping("/student-group/{studentGroupUuid}")
    public ResponseEntity<ApiResponse<List<ClassSessionResponseDTO>>> getSessionsByStudentGroup(
            @PathVariable UUID studentGroupUuid) {
        List<ClassSessionResponseDTO> sessions = classSessionService.getSessionsByStudentGroup(studentGroupUuid);
        return ResponseEntity.ok(
                ApiResponse.success(sessions, "Sesiones del grupo recuperadas con éxito")
        );
    }

    @GetMapping("/teacher/{teacherUuid}")
    public ResponseEntity<ApiResponse<List<ClassSessionResponseDTO>>> getSessionsByTeacher(
            @PathVariable UUID teacherUuid) {
        List<ClassSessionResponseDTO> sessions = classSessionService.getSessionsByTeacher(teacherUuid);
        return ResponseEntity.ok(
                ApiResponse.success(sessions, "Sesiones del docente recuperadas con éxito")
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClassSessionResponseDTO>> createClassSession(
            @Valid @RequestBody ClassSessionRequestDTO dto) {
        ClassSessionResponseDTO newSession = classSessionService.createClassSession(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(newSession, "Sesión de clase creada con éxito"));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<ClassSessionResponseDTO>> updateClassSession(
            @PathVariable UUID uuid,
            @Valid @RequestBody ClassSessionRequestDTO dto) {
        ClassSessionResponseDTO updatedSession = classSessionService.updateClassSession(uuid, dto);
        return ResponseEntity.ok(
                ApiResponse.success(updatedSession, "Sesión de clase actualizada con éxito")
        );
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteClassSession(@PathVariable UUID uuid) {
        classSessionService.deleteClassSession(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Sesión de clase eliminada con éxito")
        );
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<ClassSessionResponseDTO>>> filterClassSessions(
            @RequestParam(required = false) UUID studentGroupUuid,
            @RequestParam(required = false) UUID courseUuid,
            @RequestParam(required = false) UUID teacherUuid,
            @RequestParam(required = false) UUID learningSpaceUuid,
            @RequestParam(required = false) DayOfWeek dayOfWeek,
            @RequestParam(required = false) UUID cycleUuid,
            @RequestParam(required = false) UUID careerUuid,
            @RequestParam(required = false) UUID sessionTypeUuid) {

        ClassSessionFilterDTO filters = ClassSessionFilterDTO.builder()
                .studentGroupUuid(studentGroupUuid)
                .courseUuid(courseUuid)
                .teacherUuid(teacherUuid)
                .learningSpaceUuid(learningSpaceUuid)
                .dayOfWeek(dayOfWeek)
                .cycleUuid(cycleUuid)
                .careerUuid(careerUuid)
                .sessionTypeUuid(sessionTypeUuid)
                .build();

        List<ClassSessionResponseDTO> sessions = classSessionService.filterClassSessions(filters);
        return ResponseEntity.ok(
                ApiResponse.success(sessions, "Sesiones filtradas recuperadas con éxito")
        );
    }
}
