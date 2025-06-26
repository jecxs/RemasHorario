package com.pontificia.remashorario.modules.teacher;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.teacher.dto.*;
import com.pontificia.remashorario.modules.teacherAvailability.TeacherAvailabilityService;
import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherAvailabilityRequestDTO;
import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherAvailabilityResponseDTO;
import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherWithAvailabilitiesDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/protected/teachers")
@RequiredArgsConstructor
public class TeacherController {
    private final TeacherService teacherService;
    private final TeacherAvailabilityService availabilityService;

    // En TeacherController.java
    @GetMapping("/eligible-detailed/{courseUuid}")
    public ResponseEntity<ApiResponse<List<TeacherEligibilityResponseDTO>>> getEligibleTeachersDetailed(
            @PathVariable UUID courseUuid,
            @RequestParam(required = false) String dayOfWeek,
            @RequestParam(required = false) UUID timeSlotUuid,
            @RequestParam(required = false) String teachingHourUuids) { // ✅ NUEVO PARÁMETRO

        // Convertir string de UUIDs a lista
        List<UUID> hourUuidsList = null;
        if (teachingHourUuids != null && !teachingHourUuids.trim().isEmpty()) {
            hourUuidsList = Arrays.stream(teachingHourUuids.split(","))
                    .map(String::trim)
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
        }

        List<TeacherEligibilityResponseDTO> eligibleTeachers = teacherService
                .getEligibleTeachersWithAvailability(courseUuid, dayOfWeek, timeSlotUuid, hourUuidsList); // ✅ PASAR HORAS

        return ResponseEntity.ok(
                ApiResponse.success(eligibleTeachers, "Docentes elegibles con detalle recuperados con éxito")
        );
    }

    
    @GetMapping("/eligible/{courseUuid}")
    public ResponseEntity<ApiResponse<List<TeacherResponseDTO>>> getEligibleTeachers(
            @PathVariable UUID courseUuid,
            @RequestParam(required = false) String dayOfWeek,
            @RequestParam(required = false) UUID timeSlotUuid) {

        List<TeacherResponseDTO> eligibleTeachers = teacherService.getEligibleTeachers(
                courseUuid, dayOfWeek, timeSlotUuid);

        return ResponseEntity.ok(
                ApiResponse.success(eligibleTeachers, "Docentes elegibles recuperados con éxito")
        );
    }



    @GetMapping
    public ResponseEntity<ApiResponse<List<TeacherResponseDTO>>> getAllTeachers() {
        List<TeacherResponseDTO> teachers = teacherService.getAllTeachers();
        return ResponseEntity.ok(
                ApiResponse.success(teachers, "Docentes recuperados con éxito")
        );
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<TeacherResponseDTO>> getTeacherById(@PathVariable UUID uuid) {
        TeacherResponseDTO teacher = teacherService.getTeacherById(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(teacher, "Docente recuperado con éxito")
        );
    }

    @GetMapping("/{uuid}/details")
    public ResponseEntity<ApiResponse<TeacherWithAvailabilitiesDTO>> getTeacherWithAvailabilities(
            @PathVariable UUID uuid) {
        TeacherWithAvailabilitiesDTO teacher = teacherService.getTeacherWithAvailabilities(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(teacher, "Docente con disponibilidades recuperado con éxito")
        );
    }

    @GetMapping("/department/{departmentUuid}")
    public ResponseEntity<ApiResponse<List<TeacherResponseDTO>>> getTeachersByDepartment(
            @PathVariable UUID departmentUuid) {
        List<TeacherResponseDTO> teachers = teacherService.getTeachersByDepartment(departmentUuid);
        return ResponseEntity.ok(
                ApiResponse.success(teachers, "Docentes del departamento recuperados con éxito")
        );
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<TeacherResponseDTO>>> filterTeachers(
            @RequestParam(required = false) UUID departmentUuid,
            @RequestParam(required = false) List<UUID> knowledgeAreaUuids,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean hasUserAccount) {

        TeacherFilterDTO filters = TeacherFilterDTO.builder()
                .departmentUuid(departmentUuid)
                .knowledgeAreaUuids(knowledgeAreaUuids)
                .searchTerm(searchTerm)
                .hasUserAccount(hasUserAccount)
                .build();

        List<TeacherResponseDTO> teachers = teacherService.filterTeachers(filters);
        return ResponseEntity.ok(
                ApiResponse.success(teachers, "Docentes filtrados recuperados con éxito")
        );
    }

    @GetMapping("/course/{courseUuid}/suggested")
    public ResponseEntity<ApiResponse<List<TeacherResponseDTO>>> getSuggestedTeachersForCourse(
            @PathVariable UUID courseUuid) {
        List<TeacherResponseDTO> teachers = teacherService.getSuggestedTeachersForCourse(courseUuid);
        return ResponseEntity.ok(
                ApiResponse.success(teachers, "Docentes sugeridos para el curso recuperados con éxito")
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TeacherResponseDTO>> createTeacher(
            @Valid @RequestBody TeacherRequestDTO dto) {
        TeacherResponseDTO newTeacher = teacherService.createTeacher(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(newTeacher, "Docente creado con éxito"));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<TeacherResponseDTO>> updateTeacher(
            @PathVariable UUID uuid,
            @Valid @RequestBody TeacherUpdateDTO dto) {
        TeacherResponseDTO updatedTeacher = teacherService.updateTeacher(uuid, dto);
        return ResponseEntity.ok(
                ApiResponse.success(updatedTeacher, "Docente actualizado con éxito")
        );
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable UUID uuid) {
        teacherService.deleteTeacher(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Docente eliminado con éxito")
        );
    }

    // ===== ENDPOINTS DE DISPONIBILIDAD =====


    @GetMapping("/{teacherUuid}/availabilities")
    public ResponseEntity<ApiResponse<List<TeacherAvailabilityResponseDTO>>> getTeacherAvailabilities(
            @PathVariable UUID teacherUuid) {

        List<TeacherAvailabilityResponseDTO> availabilities =
               availabilityService.getTeacherAvailabilities(teacherUuid);

        return ResponseEntity.ok(
                ApiResponse.success(availabilities, "Disponibilidades del docente recuperadas con éxito")
        );
    }


    @GetMapping("/{teacherUuid}/availabilities/day/{dayOfWeek}")
    public ResponseEntity<ApiResponse<List<TeacherAvailabilityResponseDTO>>> getTeacherAvailabilitiesByDay(
            @PathVariable UUID teacherUuid,
            @PathVariable DayOfWeek dayOfWeek) {
        List<TeacherAvailabilityResponseDTO> availabilities =
                availabilityService.getTeacherAvailabilitiesByDay(teacherUuid, dayOfWeek);
        return ResponseEntity.ok(
                ApiResponse.success(availabilities, "Disponibilidades del día recuperadas con éxito")
        );
    }

    @PostMapping("/{teacherUuid}/availabilities")
    public ResponseEntity<ApiResponse<TeacherAvailabilityResponseDTO>> createAvailability(
            @PathVariable UUID teacherUuid,
            @Valid @RequestBody TeacherAvailabilityRequestDTO dto) {
        TeacherAvailabilityResponseDTO newAvailability =
                availabilityService.createAvailability(teacherUuid, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(newAvailability, "Disponibilidad creada con éxito"));
    }

    @PutMapping("/availabilities/{uuid}")
    public ResponseEntity<ApiResponse<TeacherAvailabilityResponseDTO>> updateAvailability(
            @PathVariable UUID uuid,
            @Valid @RequestBody TeacherAvailabilityRequestDTO dto) {
        TeacherAvailabilityResponseDTO updatedAvailability =
                availabilityService.updateAvailability(uuid, dto);
        return ResponseEntity.ok(
                ApiResponse.success(updatedAvailability, "Disponibilidad actualizada con éxito")
        );
    }

    @DeleteMapping("/availabilities/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteAvailability(@PathVariable UUID uuid) {
        availabilityService.deleteAvailability(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Disponibilidad eliminada con éxito")
        );
    }

    @DeleteMapping("/{teacherUuid}/availabilities")
    public ResponseEntity<ApiResponse<Void>> deleteAllTeacherAvailabilities(@PathVariable UUID teacherUuid) {
        availabilityService.deleteAllTeacherAvailabilities(teacherUuid);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Todas las disponibilidades del docente eliminadas con éxito")
        );
    }

    @GetMapping("/{teacherUuid}/availabilities/check")
    public ResponseEntity<ApiResponse<Boolean>> checkTeacherAvailability(
            @PathVariable UUID teacherUuid,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {

        boolean isAvailable = availabilityService.isTeacherAvailable(
                teacherUuid, dayOfWeek, startTime, endTime);

        return ResponseEntity.ok(
                ApiResponse.success(isAvailable,
                        isAvailable ? "El docente está disponible" : "El docente no está disponible")
        );
    }
}
