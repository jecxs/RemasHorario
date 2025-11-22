package com.pontificia.remashorario.modules.modalityRate;

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
 * REST Controller for managing hourly rates by educational modality
 * Different modalities (Instituto, Escuela) can have different rates
 */
@RestController
@RequestMapping("/api/protected/modality-rates")
@RequiredArgsConstructor
public class ModalityRateController {

    private final ModalityRateService modalityRateService;

    /**
     * Get all modality rates
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ModalityRateEntity>>> getAllRates() {
        List<ModalityRateEntity> rates = modalityRateService.getAllRates();
        return ResponseEntity.ok(
                ApiResponse.success(rates, "Tarifas por modalidad recuperadas con éxito")
        );
    }

    /**
     * Get modality rate by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<ModalityRateEntity>> getRateById(@PathVariable UUID uuid) {
        ModalityRateEntity rate = modalityRateService.getRateById(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(rate, "Tarifa por modalidad recuperada con éxito")
        );
    }

    /**
     * Get modality rate by ID with full details
     */
    @GetMapping("/{uuid}/details")
    public ResponseEntity<ApiResponse<ModalityRateEntity>> getRateByIdWithDetails(@PathVariable UUID uuid) {
        ModalityRateEntity rate = modalityRateService.getRateByIdWithDetails(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(rate, "Tarifa por modalidad con detalles recuperada con éxito")
        );
    }

    /**
     * Get all rates for a specific modality
     */
    @GetMapping("/modality/{modalityUuid}")
    public ResponseEntity<ApiResponse<List<ModalityRateEntity>>> getRatesByModality(
            @PathVariable UUID modalityUuid) {
        List<ModalityRateEntity> rates = modalityRateService.getRatesByModality(modalityUuid);
        return ResponseEntity.ok(
                ApiResponse.success(rates, "Tarifas de la modalidad recuperadas con éxito")
        );
    }

    /**
     * Get all rates for a specific activity type
     */
    @GetMapping("/activity-type/{activityTypeUuid}")
    public ResponseEntity<ApiResponse<List<ModalityRateEntity>>> getRatesByActivityType(
            @PathVariable UUID activityTypeUuid) {
        List<ModalityRateEntity> rates = modalityRateService.getRatesByActivityType(activityTypeUuid);
        return ResponseEntity.ok(
                ApiResponse.success(rates, "Tarifas del tipo de actividad recuperadas con éxito")
        );
    }

    /**
     * Get active rate for a modality and activity type on a specific date
     */
    @GetMapping("/modality/{modalityUuid}/activity-type/{activityTypeUuid}/active")
    public ResponseEntity<ApiResponse<ModalityRateEntity>> getActiveRateByModalityAndActivityType(
            @PathVariable UUID modalityUuid,
            @PathVariable UUID activityTypeUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        ModalityRateEntity rate = modalityRateService.getActiveRateByModalityAndActivityType(
                modalityUuid, activityTypeUuid, effectiveDate);
        return ResponseEntity.ok(
                ApiResponse.success(rate, "Tarifa activa por modalidad recuperada con éxito")
        );
    }

    /**
     * Get all active rates for a modality on a specific date
     */
    @GetMapping("/modality/{modalityUuid}/active")
    public ResponseEntity<ApiResponse<List<ModalityRateEntity>>> getActiveRatesByModality(
            @PathVariable UUID modalityUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        List<ModalityRateEntity> rates = modalityRateService.getActiveRatesByModality(modalityUuid, effectiveDate);
        return ResponseEntity.ok(
                ApiResponse.success(rates, "Tarifas activas de la modalidad recuperadas con éxito")
        );
    }

    /**
     * Get rate per minute for a modality and activity type
     */
    @GetMapping("/modality/{modalityUuid}/activity-type/{activityTypeUuid}/rate-per-minute")
    public ResponseEntity<ApiResponse<BigDecimal>> getRatePerMinute(
            @PathVariable UUID modalityUuid,
            @PathVariable UUID activityTypeUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        BigDecimal ratePerMinute = modalityRateService.getRatePerMinute(modalityUuid, activityTypeUuid, effectiveDate);
        return ResponseEntity.ok(
                ApiResponse.success(ratePerMinute, "Tarifa por minuto calculada con éxito")
        );
    }

    /**
     * Create new modality rate
     * TODO: Replace parameters with ModalityRateRequestDTO when DTOs are created
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ModalityRateEntity>> createRate(
            @RequestParam UUID modalityUuid,
            @RequestParam UUID activityTypeUuid,
            @RequestParam BigDecimal ratePerHour,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveTo) {
        ModalityRateEntity rate = modalityRateService.createRate(
                modalityUuid, activityTypeUuid, ratePerHour, effectiveFrom, effectiveTo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(rate, "Tarifa por modalidad creada con éxito"));
    }

    /**
     * Update modality rate
     * TODO: Replace parameters with ModalityRateRequestDTO when DTOs are created
     */
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<ModalityRateEntity>> updateRate(
            @PathVariable UUID uuid,
            @RequestParam BigDecimal ratePerHour,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveTo) {
        ModalityRateEntity rate = modalityRateService.updateRate(uuid, ratePerHour, effectiveFrom, effectiveTo);
        return ResponseEntity.ok(
                ApiResponse.success(rate, "Tarifa por modalidad actualizada con éxito")
        );
    }

    /**
     * Close a rate by setting its effectiveTo date to today
     */
    @PatchMapping("/{uuid}/close")
    public ResponseEntity<ApiResponse<ModalityRateEntity>> closeRate(@PathVariable UUID uuid) {
        ModalityRateEntity rate = modalityRateService.closeRate(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(rate, "Tarifa por modalidad cerrada con éxito")
        );
    }

    /**
     * Create a new rate version (closes previous and creates new)
     */
    @PostMapping("/modality/{modalityUuid}/activity-type/{activityTypeUuid}/new-version")
    public ResponseEntity<ApiResponse<ModalityRateEntity>> createNewRateVersion(
            @PathVariable UUID modalityUuid,
            @PathVariable UUID activityTypeUuid,
            @RequestParam BigDecimal newRatePerHour,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom) {
        ModalityRateEntity rate = modalityRateService.createNewRateVersion(
                modalityUuid, activityTypeUuid, newRatePerHour, effectiveFrom);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(rate, "Nueva versión de tarifa por modalidad creada con éxito"));
    }

    /**
     * Delete modality rate
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteRate(@PathVariable UUID uuid) {
        modalityRateService.deleteRate(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Tarifa por modalidad eliminada con éxito")
        );
    }
}
