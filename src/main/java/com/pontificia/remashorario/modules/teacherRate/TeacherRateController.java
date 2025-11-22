package com.pontificia.remashorario.modules.teacherRate;

import com.pontificia.remashorario.config.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing teacher-specific hourly rates
 * These rates override modality and default rates for specific teachers
 */
@RestController
@RequestMapping("/api/protected/teacher-rates")
@RequiredArgsConstructor
public class TeacherRateController {

    private final TeacherRateService teacherRateService;

    /**
     * Get all teacher rates
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TeacherRateEntity>>> getAllRates() {
        List<TeacherRateEntity> rates = teacherRateService.getAllRates();
        return ResponseEntity.ok(
                ApiResponse.success(rates, "Tarifas de docentes recuperadas con éxito")
        );
    }

    /**
     * Get teacher rate by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<TeacherRateEntity>> getRateById(@PathVariable UUID uuid) {
        TeacherRateEntity rate = teacherRateService.getRateById(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(rate, "Tarifa de docente recuperada con éxito")
        );
    }

    /**
     * Get teacher rate by ID with full details
     */
    @GetMapping("/{uuid}/details")
    public ResponseEntity<ApiResponse<TeacherRateEntity>> getRateByIdWithDetails(@PathVariable UUID uuid) {
        TeacherRateEntity rate = teacherRateService.getRateByIdWithDetails(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(rate, "Tarifa de docente con detalles recuperada con éxito")
        );
    }

    /**
     * Get all rates for a specific teacher
     */
    @GetMapping("/teacher/{teacherUuid}")
    public ResponseEntity<ApiResponse<List<TeacherRateEntity>>> getRatesByTeacher(
            @PathVariable UUID teacherUuid) {
        List<TeacherRateEntity> rates = teacherRateService.getRatesByTeacher(teacherUuid);
        return ResponseEntity.ok(
                ApiResponse.success(rates, "Tarifas del docente recuperadas con éxito")
        );
    }

    /**
     * Get all rates for a specific activity type
     */
    @GetMapping("/activity-type/{activityTypeUuid}")
    public ResponseEntity<ApiResponse<List<TeacherRateEntity>>> getRatesByActivityType(
            @PathVariable UUID activityTypeUuid) {
        List<TeacherRateEntity> rates = teacherRateService.getRatesByActivityType(activityTypeUuid);
        return ResponseEntity.ok(
                ApiResponse.success(rates, "Tarifas del tipo de actividad recuperadas con éxito")
        );
    }

    /**
     * Get active rate for a teacher and activity type on a specific date
     */
    @GetMapping("/teacher/{teacherUuid}/activity-type/{activityTypeUuid}/active")
    public ResponseEntity<ApiResponse<TeacherRateEntity>> getActiveRateByTeacherAndActivityType(
            @PathVariable UUID teacherUuid,
            @PathVariable UUID activityTypeUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        TeacherRateEntity rate = teacherRateService.getActiveRateByTeacherAndActivityType(
                teacherUuid, activityTypeUuid, effectiveDate);
        return ResponseEntity.ok(
                ApiResponse.success(rate, "Tarifa activa del docente recuperada con éxito")
        );
    }

    /**
     * Get all active rates for a teacher on a specific date
     */
    @GetMapping("/teacher/{teacherUuid}/active")
    public ResponseEntity<ApiResponse<List<TeacherRateEntity>>> getActiveRatesByTeacher(
            @PathVariable UUID teacherUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        List<TeacherRateEntity> rates = teacherRateService.getActiveRatesByTeacher(teacherUuid, effectiveDate);
        return ResponseEntity.ok(
                ApiResponse.success(rates, "Tarifas activas del docente recuperadas con éxito")
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
     * TODO: Replace parameters with TeacherRateRequestDTO when DTOs are created
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TeacherRateEntity>> createRate(
            @RequestParam UUID teacherUuid,
            @RequestParam UUID activityTypeUuid,
            @RequestParam BigDecimal ratePerHour,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveTo) {
        TeacherRateEntity rate = teacherRateService.createRate(
                teacherUuid, activityTypeUuid, ratePerHour, effectiveFrom, effectiveTo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(rate, "Tarifa de docente creada con éxito"));
    }

    /**
     * Update teacher rate
     * TODO: Replace parameters with TeacherRateRequestDTO when DTOs are created
     */
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<TeacherRateEntity>> updateRate(
            @PathVariable UUID uuid,
            @RequestParam BigDecimal ratePerHour,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveTo) {
        TeacherRateEntity rate = teacherRateService.updateRate(uuid, ratePerHour, effectiveFrom, effectiveTo);
        return ResponseEntity.ok(
                ApiResponse.success(rate, "Tarifa de docente actualizada con éxito")
        );
    }

    /**
     * Close a rate by setting its effectiveTo date to today
     */
    @PatchMapping("/{uuid}/close")
    public ResponseEntity<ApiResponse<TeacherRateEntity>> closeRate(@PathVariable UUID uuid) {
        TeacherRateEntity rate = teacherRateService.closeRate(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(rate, "Tarifa de docente cerrada con éxito")
        );
    }

    /**
     * Create a new rate version (closes previous and creates new)
     */
    @PostMapping("/teacher/{teacherUuid}/activity-type/{activityTypeUuid}/new-version")
    public ResponseEntity<ApiResponse<TeacherRateEntity>> createNewRateVersion(
            @PathVariable UUID teacherUuid,
            @PathVariable UUID activityTypeUuid,
            @RequestParam BigDecimal newRatePerHour,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom) {
        TeacherRateEntity rate = teacherRateService.createNewRateVersion(
                teacherUuid, activityTypeUuid, newRatePerHour, effectiveFrom);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(rate, "Nueva versión de tarifa de docente creada con éxito"));
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
     * TODO: Replace List<TeacherRateEntity> with BulkTeacherRateRequestDTO when DTOs are created
     */
    @PostMapping("/teacher/{teacherUuid}/bulk")
    public ResponseEntity<ApiResponse<List<TeacherRateEntity>>> createBulkRatesForTeacher(
            @PathVariable UUID teacherUuid,
            @RequestBody List<TeacherRateEntity> rates,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom) {
        List<TeacherRateEntity> created = teacherRateService.createBulkRatesForTeacher(teacherUuid, rates, effectiveFrom);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Tarifas de docente creadas en masa con éxito"));
    }
}
