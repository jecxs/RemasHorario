package com.pontificia.remashorario.modules.attendanceActivityType;

import com.pontificia.remashorario.config.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing attendance activity types
 * Handles CRUD operations for activity types like Regular Class, Workshop, Substitute Exam, etc.
 */
@RestController
@RequestMapping("/api/protected/attendance-activity-types")
@RequiredArgsConstructor
public class AttendanceActivityTypeController {

    private final AttendanceActivityTypeService activityTypeService;

    /**
     * Get all activity types
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AttendanceActivityTypeEntity>>> getAllActivityTypes() {
        List<AttendanceActivityTypeEntity> activityTypes = activityTypeService.getAllActivityTypes();
        return ResponseEntity.ok(
                ApiResponse.success(activityTypes, "Tipos de actividad recuperados con éxito")
        );
    }

    /**
     * Get activity type by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<AttendanceActivityTypeEntity>> getActivityTypeById(@PathVariable UUID uuid) {
        AttendanceActivityTypeEntity activityType = activityTypeService.getActivityTypeById(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(activityType, "Tipo de actividad recuperado con éxito")
        );
    }

    /**
     * Get activity type by code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<AttendanceActivityTypeEntity>> getActivityTypeByCode(@PathVariable String code) {
        AttendanceActivityTypeEntity activityType = activityTypeService.getActivityTypeByCode(code);
        return ResponseEntity.ok(
                ApiResponse.success(activityType, "Tipo de actividad recuperado con éxito")
        );
    }

    /**
     * Create new activity type
     * TODO: Replace parameters with ActivityTypeRequestDTO when DTOs are created
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceActivityTypeEntity>> createActivityType(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        AttendanceActivityTypeEntity activityType = activityTypeService.createActivityType(code, name, description);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(activityType, "Tipo de actividad creado con éxito"));
    }

    /**
     * Update activity type
     * TODO: Replace parameters with ActivityTypeRequestDTO when DTOs are created
     */
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<AttendanceActivityTypeEntity>> updateActivityType(
            @PathVariable UUID uuid,
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        AttendanceActivityTypeEntity activityType = activityTypeService.updateActivityType(uuid, code, name, description);
        return ResponseEntity.ok(
                ApiResponse.success(activityType, "Tipo de actividad actualizado con éxito")
        );
    }

    /**
     * Delete activity type
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteActivityType(@PathVariable UUID uuid) {
        activityTypeService.deleteActivityType(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Tipo de actividad eliminado con éxito")
        );
    }

    /**
     * Initialize default activity types
     * Useful for initial system setup
     */
    @PostMapping("/initialize-defaults")
    public ResponseEntity<ApiResponse<Void>> createDefaultActivityTypes() {
        activityTypeService.createDefaultActivityTypes();
        return ResponseEntity.ok(
                ApiResponse.success(null, "Tipos de actividad por defecto creados con éxito")
        );
    }
}
