package com.pontificia.remashorario.modules.defaultRate;

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
 * REST Controller for managing default hourly rates
 * These are fallback rates used when no specific teacher or modality rate is defined
 */
@RestController
@RequestMapping("/api/protected/default-rates")
@RequiredArgsConstructor
public class DefaultRateController {

    private final DefaultRateService defaultRateService;

    /**
     * Get all default rates
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DefaultRateEntity>>> getAllRates() {
        List<DefaultRateEntity> rates = defaultRateService.getAllRates();
        return ResponseEntity.ok(
                ApiResponse.success(rates, "Tarifas por defecto recuperadas con éxito")
        );
    }

    /**
     * Get default rate by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<DefaultRateEntity>> getRateById(@PathVariable UUID uuid) {
        DefaultRateEntity rate = defaultRateService.getRateById(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(rate, "Tarifa por defecto recuperada con éxito")
        );
    }

    /**
     * Get default rate by ID with full details (includes activity type)
     */
    @GetMapping("/{uuid}/details")
    public ResponseEntity<ApiResponse<DefaultRateEntity>> getRateByIdWithDetails(@PathVariable UUID uuid) {
        DefaultRateEntity rate = defaultRateService.getRateByIdWithDetails(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(rate, "Tarifa por defecto con detalles recuperada con éxito")
        );
    }

    /**
     * Get all rates for a specific activity type
     */
    @GetMapping("/activity-type/{activityTypeUuid}")
    public ResponseEntity<ApiResponse<List<DefaultRateEntity>>> getRatesByActivityType(
            @PathVariable UUID activityTypeUuid) {
        List<DefaultRateEntity> rates = defaultRateService.getRatesByActivityType(activityTypeUuid);
        return ResponseEntity.ok(
                ApiResponse.success(rates, "Tarifas por defecto del tipo de actividad recuperadas con éxito")
        );
    }

    /**
     * Get active rate for an activity type on a specific date
     */
    @GetMapping("/activity-type/{activityTypeUuid}/active")
    public ResponseEntity<ApiResponse<DefaultRateEntity>> getActiveRateByActivityType(
            @PathVariable UUID activityTypeUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        DefaultRateEntity rate = defaultRateService.getActiveRateByActivityType(activityTypeUuid, effectiveDate);
        return ResponseEntity.ok(
                ApiResponse.success(rate, "Tarifa activa por defecto recuperada con éxito")
        );
    }

    /**
     * Get all active rates for a specific date
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<DefaultRateEntity>>> getActiveRates(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        List<DefaultRateEntity> rates = defaultRateService.getActiveRates(effectiveDate);
        return ResponseEntity.ok(
                ApiResponse.success(rates, "Tarifas activas por defecto recuperadas con éxito")
        );
    }

    /**
     * Get rate per minute for an activity type
     */
    @GetMapping("/activity-type/{activityTypeUuid}/rate-per-minute")
    public ResponseEntity<ApiResponse<BigDecimal>> getRatePerMinute(
            @PathVariable UUID activityTypeUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        BigDecimal ratePerMinute = defaultRateService.getRatePerMinute(activityTypeUuid, effectiveDate);
        return ResponseEntity.ok(
                ApiResponse.success(ratePerMinute, "Tarifa por minuto calculada con éxito")
        );
    }

    /**
     * Create new default rate
     * TODO: Replace parameters with DefaultRateRequestDTO when DTOs are created
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DefaultRateEntity>> createRate(
            @RequestParam UUID activityTypeUuid,
            @RequestParam BigDecimal ratePerHour,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveTo) {
        DefaultRateEntity rate = defaultRateService.createRate(activityTypeUuid, ratePerHour, effectiveFrom, effectiveTo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(rate, "Tarifa por defecto creada con éxito"));
    }

    /**
     * Update default rate
     * TODO: Replace parameters with DefaultRateRequestDTO when DTOs are created
     */
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<DefaultRateEntity>> updateRate(
            @PathVariable UUID uuid,
            @RequestParam BigDecimal ratePerHour,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveTo) {
        DefaultRateEntity rate = defaultRateService.updateRate(uuid, ratePerHour, effectiveFrom, effectiveTo);
        return ResponseEntity.ok(
                ApiResponse.success(rate, "Tarifa por defecto actualizada con éxito")
        );
    }

    /**
     * Close a rate by setting its effectiveTo date to today
     */
    @PatchMapping("/{uuid}/close")
    public ResponseEntity<ApiResponse<DefaultRateEntity>> closeRate(@PathVariable UUID uuid) {
        DefaultRateEntity rate = defaultRateService.closeRate(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(rate, "Tarifa por defecto cerrada con éxito")
        );
    }

    /**
     * Create a new rate version (closes previous and creates new)
     */
    @PostMapping("/activity-type/{activityTypeUuid}/new-version")
    public ResponseEntity<ApiResponse<DefaultRateEntity>> createNewRateVersion(
            @PathVariable UUID activityTypeUuid,
            @RequestParam BigDecimal newRatePerHour,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom) {
        DefaultRateEntity rate = defaultRateService.createNewRateVersion(activityTypeUuid, newRatePerHour, effectiveFrom);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(rate, "Nueva versión de tarifa por defecto creada con éxito"));
    }

    /**
     * Delete default rate
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteRate(@PathVariable UUID uuid) {
        defaultRateService.deleteRate(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Tarifa por defecto eliminada con éxito")
        );
    }
}
