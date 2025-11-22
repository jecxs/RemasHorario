package com.pontificia.remashorario.modules.defaultRate;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.defaultRate.dto.DefaultRateRequestDTO;
import com.pontificia.remashorario.modules.defaultRate.dto.DefaultRateResponseDTO;
import com.pontificia.remashorario.modules.defaultRate.mapper.DefaultRateMapper;
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

/**
 * REST Controller for managing default hourly rates
 * These are fallback rates used when no specific teacher or modality rate is defined
 */
@RestController
@RequestMapping("/api/protected/default-rates")
@RequiredArgsConstructor
public class DefaultRateController {

    private final DefaultRateService defaultRateService;
    private final DefaultRateMapper defaultRateMapper;

    /**
     * Get all default rates
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DefaultRateResponseDTO>>> getAllRates() {
        List<DefaultRateEntity> rates = defaultRateService.getAllRates();
        List<DefaultRateResponseDTO> responseDTOs = defaultRateMapper.toResponseDTOList(rates);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Tarifas por defecto recuperadas con éxito")
        );
    }

    /**
     * Get default rate by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<DefaultRateResponseDTO>> getRateById(@PathVariable UUID uuid) {
        DefaultRateEntity rate = defaultRateService.getRateById(uuid);
        DefaultRateResponseDTO responseDTO = defaultRateMapper.toResponseDTO(rate);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tarifa por defecto recuperada con éxito")
        );
    }

    /**
     * Get default rate by ID with full details (includes activity type)
     */
    @GetMapping("/{uuid}/details")
    public ResponseEntity<ApiResponse<DefaultRateResponseDTO>> getRateByIdWithDetails(@PathVariable UUID uuid) {
        DefaultRateEntity rate = defaultRateService.getRateByIdWithDetails(uuid);
        DefaultRateResponseDTO responseDTO = defaultRateMapper.toResponseDTO(rate);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tarifa por defecto con detalles recuperada con éxito")
        );
    }

    /**
     * Get all rates for a specific activity type
     */
    @GetMapping("/activity-type/{activityTypeUuid}")
    public ResponseEntity<ApiResponse<List<DefaultRateResponseDTO>>> getRatesByActivityType(
            @PathVariable UUID activityTypeUuid) {
        List<DefaultRateEntity> rates = defaultRateService.getRatesByActivityType(activityTypeUuid);
        List<DefaultRateResponseDTO> responseDTOs = defaultRateMapper.toResponseDTOList(rates);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Tarifas por defecto del tipo de actividad recuperadas con éxito")
        );
    }

    /**
     * Get active rate for an activity type on a specific date
     */
    @GetMapping("/activity-type/{activityTypeUuid}/active")
    public ResponseEntity<ApiResponse<DefaultRateResponseDTO>> getActiveRateByActivityType(
            @PathVariable UUID activityTypeUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        DefaultRateEntity rate = defaultRateService.getActiveRateByActivityType(activityTypeUuid, effectiveDate);
        DefaultRateResponseDTO responseDTO = defaultRateMapper.toResponseDTO(rate);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tarifa activa por defecto recuperada con éxito")
        );
    }

    /**
     * Get all active rates for a specific date
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<DefaultRateResponseDTO>>> getActiveRates(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        List<DefaultRateEntity> rates = defaultRateService.getActiveRates(effectiveDate);
        List<DefaultRateResponseDTO> responseDTOs = defaultRateMapper.toResponseDTOList(rates);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Tarifas activas por defecto recuperadas con éxito")
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
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DefaultRateResponseDTO>> createRate(
            @Valid @RequestBody DefaultRateRequestDTO requestDTO) {
        DefaultRateEntity rate = defaultRateService.createRate(
                requestDTO.getActivityTypeUuid(),
                requestDTO.getRatePerHour(),
                requestDTO.getEffectiveFrom(),
                requestDTO.getEffectiveTo()
        );
        DefaultRateResponseDTO responseDTO = defaultRateMapper.toResponseDTO(rate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTO, "Tarifa por defecto creada con éxito"));
    }

    /**
     * Update default rate
     */
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<DefaultRateResponseDTO>> updateRate(
            @PathVariable UUID uuid,
            @Valid @RequestBody DefaultRateRequestDTO requestDTO) {
        DefaultRateEntity rate = defaultRateService.updateRate(
                uuid,
                requestDTO.getRatePerHour(),
                requestDTO.getEffectiveFrom(),
                requestDTO.getEffectiveTo()
        );
        DefaultRateResponseDTO responseDTO = defaultRateMapper.toResponseDTO(rate);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tarifa por defecto actualizada con éxito")
        );
    }

    /**
     * Close a rate by setting its effectiveTo date to today
     */
    @PatchMapping("/{uuid}/close")
    public ResponseEntity<ApiResponse<DefaultRateResponseDTO>> closeRate(@PathVariable UUID uuid) {
        DefaultRateEntity rate = defaultRateService.closeRate(uuid);
        DefaultRateResponseDTO responseDTO = defaultRateMapper.toResponseDTO(rate);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tarifa por defecto cerrada con éxito")
        );
    }

    /**
     * Create a new rate version (closes previous and creates new)
     */
    @PostMapping("/activity-type/{activityTypeUuid}/new-version")
    public ResponseEntity<ApiResponse<DefaultRateResponseDTO>> createNewRateVersion(
            @PathVariable UUID activityTypeUuid,
            @RequestParam BigDecimal newRatePerHour,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom) {
        DefaultRateEntity rate = defaultRateService.createNewRateVersion(activityTypeUuid, newRatePerHour, effectiveFrom);
        DefaultRateResponseDTO responseDTO = defaultRateMapper.toResponseDTO(rate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTO, "Nueva versión de tarifa por defecto creada con éxito"));
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
