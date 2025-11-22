package com.pontificia.remashorario.modules.teacherRate;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.teacherRate.dto.TeacherRateRequestDTO;
import com.pontificia.remashorario.modules.teacherRate.dto.TeacherRateResponseDTO;
import com.pontificia.remashorario.modules.teacherRate.mapper.TeacherRateMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for managing teacher-specific hourly rates
 * These rates override modality and default rates for specific teachers
 */
@RestController
@RequestMapping("/api/protected/teacher-rates")
@RequiredArgsConstructor
public class TeacherRateController {

    private final TeacherRateService teacherRateService;
    private final TeacherRateMapper teacherRateMapper;

    /**
     * Get all teacher rates
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TeacherRateResponseDTO>>> getAllRates() {
        List<TeacherRateEntity> rates = teacherRateService.getAllRates();
        List<TeacherRateResponseDTO> responseDTOs = teacherRateMapper.toResponseDTOList(rates);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Tarifas de docentes recuperadas con éxito")
        );
    }

    /**
     * Get teacher rate by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<TeacherRateResponseDTO>> getRateById(@PathVariable UUID uuid) {
        TeacherRateEntity rate = teacherRateService.getRateById(uuid);
        TeacherRateResponseDTO responseDTO = teacherRateMapper.toResponseDTO(rate);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tarifa de docente recuperada con éxito")
        );
    }

    /**
     * Get teacher rate by ID with full details
     */
    @GetMapping("/{uuid}/details")
    public ResponseEntity<ApiResponse<TeacherRateResponseDTO>> getRateByIdWithDetails(@PathVariable UUID uuid) {
        TeacherRateEntity rate = teacherRateService.getRateByIdWithDetails(uuid);
        TeacherRateResponseDTO responseDTO = teacherRateMapper.toResponseDTO(rate);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tarifa de docente con detalles recuperada con éxito")
        );
    }

    /**
     * Get all rates for a specific teacher
     */
    @GetMapping("/teacher/{teacherUuid}")
    public ResponseEntity<ApiResponse<List<TeacherRateResponseDTO>>> getRatesByTeacher(
            @PathVariable UUID teacherUuid) {
        List<TeacherRateEntity> rates = teacherRateService.getRatesByTeacher(teacherUuid);
        List<TeacherRateResponseDTO> responseDTOs = teacherRateMapper.toResponseDTOList(rates);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Tarifas del docente recuperadas con éxito")
        );
    }

    /**
     * Get all rates for a specific activity type
     */
    @GetMapping("/activity-type/{activityTypeUuid}")
    public ResponseEntity<ApiResponse<List<TeacherRateResponseDTO>>> getRatesByActivityType(
            @PathVariable UUID activityTypeUuid) {
        List<TeacherRateEntity> rates = teacherRateService.getRatesByActivityType(activityTypeUuid);
        List<TeacherRateResponseDTO> responseDTOs = teacherRateMapper.toResponseDTOList(rates);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Tarifas del tipo de actividad recuperadas con éxito")
        );
    }

    /**
     * Get active rate for a teacher and activity type on a specific date
     */
    @GetMapping("/teacher/{teacherUuid}/activity-type/{activityTypeUuid}/active")
    public ResponseEntity<ApiResponse<TeacherRateResponseDTO>> getActiveRateByTeacherAndActivityType(
            @PathVariable UUID teacherUuid,
            @PathVariable UUID activityTypeUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        TeacherRateEntity rate = teacherRateService.getActiveRateByTeacherAndActivityType(
                teacherUuid, activityTypeUuid, effectiveDate);
        TeacherRateResponseDTO responseDTO = teacherRateMapper.toResponseDTO(rate);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tarifa activa del docente recuperada con éxito")
        );
    }

    /**
     * Get all active rates for a teacher on a specific date
     */
    @GetMapping("/teacher/{teacherUuid}/active")
    public ResponseEntity<ApiResponse<List<TeacherRateResponseDTO>>> getActiveRatesByTeacher(
            @PathVariable UUID teacherUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        List<TeacherRateEntity> rates = teacherRateService.getActiveRatesByTeacher(teacherUuid, effectiveDate);
        List<TeacherRateResponseDTO> responseDTOs = teacherRateMapper.toResponseDTOList(rates);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Tarifas activas del docente recuperadas con éxito")
        );
    }

    /**
     * Check if a teacher has a specific rate for an activity type
     */
    @GetMapping("/teacher/{teacherUuid}/activity-type/{activityTypeUuid}/has-specific-rate")
    public ResponseEntity<ApiResponse<Boolean>> hasSpecificRate(
            @PathVariable UUID teacherUuid,
            @PathVariable UUID activityTypeUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        boolean hasRate = teacherRateService.hasSpecificRate(teacherUuid, activityTypeUuid, effectiveDate);
        return ResponseEntity.ok(
                ApiResponse.success(hasRate, "Verificación de tarifa específica realizada con éxito")
        );
    }

    /**
     * Get rate per minute for a teacher and activity type
     */
    @GetMapping("/teacher/{teacherUuid}/activity-type/{activityTypeUuid}/rate-per-minute")
    public ResponseEntity<ApiResponse<BigDecimal>> getRatePerMinute(
            @PathVariable UUID teacherUuid,
            @PathVariable UUID activityTypeUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        BigDecimal ratePerMinute = teacherRateService.getRatePerMinute(teacherUuid, activityTypeUuid, effectiveDate);
        return ResponseEntity.ok(
                ApiResponse.success(ratePerMinute, "Tarifa por minuto calculada con éxito")
        );
    }

    /**
     * Create new teacher rate
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TeacherRateResponseDTO>> createRate(
            @Valid @RequestBody TeacherRateRequestDTO requestDTO) {
        TeacherRateEntity rate = teacherRateService.createRate(
                requestDTO.getTeacherUuid(),
                requestDTO.getActivityTypeUuid(),
                requestDTO.getRatePerHour(),
                requestDTO.getEffectiveFrom(),
                requestDTO.getEffectiveTo()
        );
        TeacherRateResponseDTO responseDTO = teacherRateMapper.toResponseDTO(rate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTO, "Tarifa de docente creada con éxito"));
    }

    /**
     * Update teacher rate
     */
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<TeacherRateResponseDTO>> updateRate(
            @PathVariable UUID uuid,
            @Valid @RequestBody TeacherRateRequestDTO requestDTO) {
        TeacherRateEntity rate = teacherRateService.updateRate(
                uuid,
                requestDTO.getRatePerHour(),
                requestDTO.getEffectiveFrom(),
                requestDTO.getEffectiveTo()
        );
        TeacherRateResponseDTO responseDTO = teacherRateMapper.toResponseDTO(rate);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tarifa de docente actualizada con éxito")
        );
    }

    /**
     * Close a rate by setting its effectiveTo date to today
     */
    @PatchMapping("/{uuid}/close")
    public ResponseEntity<ApiResponse<TeacherRateResponseDTO>> closeRate(@PathVariable UUID uuid) {
        TeacherRateEntity rate = teacherRateService.closeRate(uuid);
        TeacherRateResponseDTO responseDTO = teacherRateMapper.toResponseDTO(rate);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tarifa de docente cerrada con éxito")
        );
    }

    /**
     * Create a new rate version (closes previous and creates new)
     */
    @PostMapping("/teacher/{teacherUuid}/activity-type/{activityTypeUuid}/new-version")
    public ResponseEntity<ApiResponse<TeacherRateResponseDTO>> createNewRateVersion(
            @PathVariable UUID teacherUuid,
            @PathVariable UUID activityTypeUuid,
            @RequestParam BigDecimal newRatePerHour,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom) {
        TeacherRateEntity rate = teacherRateService.createNewRateVersion(
                teacherUuid, activityTypeUuid, newRatePerHour, effectiveFrom);
        TeacherRateResponseDTO responseDTO = teacherRateMapper.toResponseDTO(rate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTO, "Nueva versión de tarifa de docente creada con éxito"));
    }

    /**
     * Delete teacher rate
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteRate(@PathVariable UUID uuid) {
        teacherRateService.deleteRate(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Tarifa de docente eliminada con éxito")
        );
    }

    /**
     * Bulk create rates for a teacher across multiple activity types
     */
    @PostMapping("/teacher/{teacherUuid}/bulk")
    public ResponseEntity<ApiResponse<List<TeacherRateResponseDTO>>> createBulkRatesForTeacher(
            @PathVariable UUID teacherUuid,
            @Valid @RequestBody List<TeacherRateRequestDTO> rateRequests,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom) {
        List<TeacherRateEntity> ratesToCreate = rateRequests.stream()
                .map(dto -> {
                    TeacherRateEntity entity = new TeacherRateEntity();
                    entity.setRatePerHour(dto.getRatePerHour());
                    // ActivityTypeUuid from DTO will be used by service
                    return entity;
                })
                .collect(Collectors.toList());

        List<TeacherRateEntity> created = teacherRateService.createBulkRatesForTeacher(teacherUuid, ratesToCreate, effectiveFrom);
        List<TeacherRateResponseDTO> responseDTOs = teacherRateMapper.toResponseDTOList(created);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTOs, "Tarifas de docente creadas en masa con éxito"));
    }
}
