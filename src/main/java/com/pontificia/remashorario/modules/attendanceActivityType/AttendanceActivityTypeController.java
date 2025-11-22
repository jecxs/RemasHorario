package com.pontificia.remashorario.modules.attendanceActivityType;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.attendanceActivityType.dto.AttendanceActivityTypeRequestDTO;
import com.pontificia.remashorario.modules.attendanceActivityType.dto.AttendanceActivityTypeResponseDTO;
import com.pontificia.remashorario.modules.attendanceActivityType.mapper.AttendanceActivityTypeMapper;
import jakarta.validation.Valid;
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
    private final AttendanceActivityTypeMapper activityTypeMapper;

    /**
     * Get all activity types
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AttendanceActivityTypeResponseDTO>>> getAllActivityTypes() {
        List<AttendanceActivityTypeEntity> activityTypes = activityTypeService.getAllActivityTypes();
        List<AttendanceActivityTypeResponseDTO> responseDTOs = activityTypeMapper.toResponseDTOList(activityTypes);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Tipos de actividad recuperados con éxito")
        );
    }

    /**
     * Get activity type by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<AttendanceActivityTypeResponseDTO>> getActivityTypeById(@PathVariable UUID uuid) {
        AttendanceActivityTypeEntity activityType = activityTypeService.getActivityTypeById(uuid);
        AttendanceActivityTypeResponseDTO responseDTO = activityTypeMapper.toResponseDTO(activityType);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tipo de actividad recuperado con éxito")
        );
    }

    /**
     * Get activity type by code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<AttendanceActivityTypeResponseDTO>> getActivityTypeByCode(@PathVariable String code) {
        AttendanceActivityTypeEntity activityType = activityTypeService.getActivityTypeByCode(code);
        AttendanceActivityTypeResponseDTO responseDTO = activityTypeMapper.toResponseDTO(activityType);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tipo de actividad recuperado con éxito")
        );
    }

    /**
     * Create new activity type
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceActivityTypeResponseDTO>> createActivityType(
            @Valid @RequestBody AttendanceActivityTypeRequestDTO requestDTO) {
        AttendanceActivityTypeEntity activityType = activityTypeService.createActivityType(
                requestDTO.getCode(),
                requestDTO.getName(),
                requestDTO.getDescription()
        );
        AttendanceActivityTypeResponseDTO responseDTO = activityTypeMapper.toResponseDTO(activityType);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTO, "Tipo de actividad creado con éxito"));
    }

    /**
     * Update activity type
     */
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<AttendanceActivityTypeResponseDTO>> updateActivityType(
            @PathVariable UUID uuid,
            @Valid @RequestBody AttendanceActivityTypeRequestDTO requestDTO) {
        AttendanceActivityTypeEntity activityType = activityTypeService.updateActivityType(
                uuid,
                requestDTO.getCode(),
                requestDTO.getName(),
                requestDTO.getDescription()
        );
        AttendanceActivityTypeResponseDTO responseDTO = activityTypeMapper.toResponseDTO(activityType);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tipo de actividad actualizado con éxito")
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
