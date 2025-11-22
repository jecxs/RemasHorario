package com.pontificia.remashorario.modules.modalityRate;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.modalityRate.dto.ModalityRateRequestDTO;
import com.pontificia.remashorario.modules.modalityRate.dto.ModalityRateResponseDTO;
import com.pontificia.remashorario.modules.modalityRate.mapper.ModalityRateMapper;
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
 * REST Controller for managing hourly rates by educational modality
 * Different modalities (Instituto, Escuela) can have different rates
 */
@RestController
@RequestMapping("/api/protected/modality-rates")
@RequiredArgsConstructor
public class ModalityRateController {

    private final ModalityRateService modalityRateService;
    private final ModalityRateMapper modalityRateMapper;

    /**
     * Get all modality rates
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ModalityRateResponseDTO>>> getAllRates() {
        List<ModalityRateEntity> rates = modalityRateService.getAllRates();
        List<ModalityRateResponseDTO> responseDTOs = modalityRateMapper.toResponseDTOList(rates);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Tarifas por modalidad recuperadas con éxito")
        );
    }

    /**
     * Get modality rate by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<ModalityRateResponseDTO>> getRateById(@PathVariable UUID uuid) {
        ModalityRateEntity rate = modalityRateService.getRateById(uuid);
        ModalityRateResponseDTO responseDTO = modalityRateMapper.toResponseDTO(rate);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tarifa por modalidad recuperada con éxito")
        );
    }

    /**
     * Get modality rate by ID with full details
     */
    @GetMapping("/{uuid}/details")
    public ResponseEntity<ApiResponse<ModalityRateResponseDTO>> getRateByIdWithDetails(@PathVariable UUID uuid) {
        ModalityRateEntity rate = modalityRateService.getRateByIdWithDetails(uuid);
        ModalityRateResponseDTO responseDTO = modalityRateMapper.toResponseDTO(rate);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tarifa por modalidad con detalles recuperada con éxito")
        );
    }

    /**
     * Get all rates for a specific modality
     */
    @GetMapping("/modality/{modalityUuid}")
    public ResponseEntity<ApiResponse<List<ModalityRateResponseDTO>>> getRatesByModality(
            @PathVariable UUID modalityUuid) {
        List<ModalityRateEntity> rates = modalityRateService.getRatesByModality(modalityUuid);
        List<ModalityRateResponseDTO> responseDTOs = modalityRateMapper.toResponseDTOList(rates);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Tarifas de la modalidad recuperadas con éxito")
        );
    }

    /**
     * Get all rates for a specific activity type
     */
    @GetMapping("/activity-type/{activityTypeUuid}")
    public ResponseEntity<ApiResponse<List<ModalityRateResponseDTO>>> getRatesByActivityType(
            @PathVariable UUID activityTypeUuid) {
        List<ModalityRateEntity> rates = modalityRateService.getRatesByActivityType(activityTypeUuid);
        List<ModalityRateResponseDTO> responseDTOs = modalityRateMapper.toResponseDTOList(rates);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Tarifas del tipo de actividad recuperadas con éxito")
        );
    }

    /**
     * Get active rate for a modality and activity type on a specific date
     */
    @GetMapping("/modality/{modalityUuid}/activity-type/{activityTypeUuid}/active")
    public ResponseEntity<ApiResponse<ModalityRateResponseDTO>> getActiveRateByModalityAndActivityType(
            @PathVariable UUID modalityUuid,
            @PathVariable UUID activityTypeUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        ModalityRateEntity rate = modalityRateService.getActiveRateByModalityAndActivityType(
                modalityUuid, activityTypeUuid, effectiveDate);
        ModalityRateResponseDTO responseDTO = modalityRateMapper.toResponseDTO(rate);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tarifa activa por modalidad recuperada con éxito")
        );
    }

    /**
     * Get all active rates for a modality on a specific date
     */
    @GetMapping("/modality/{modalityUuid}/active")
    public ResponseEntity<ApiResponse<List<ModalityRateResponseDTO>>> getActiveRatesByModality(
            @PathVariable UUID modalityUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        List<ModalityRateEntity> rates = modalityRateService.getActiveRatesByModality(modalityUuid, effectiveDate);
        List<ModalityRateResponseDTO> responseDTOs = modalityRateMapper.toResponseDTOList(rates);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Tarifas activas de la modalidad recuperadas con éxito")
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
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ModalityRateResponseDTO>> createRate(
            @Valid @RequestBody ModalityRateRequestDTO requestDTO) {
        ModalityRateEntity rate = modalityRateService.createRate(
                requestDTO.getModalityUuid(),
                requestDTO.getActivityTypeUuid(),
                requestDTO.getRatePerHour(),
                requestDTO.getEffectiveFrom(),
                requestDTO.getEffectiveTo()
        );
        ModalityRateResponseDTO responseDTO = modalityRateMapper.toResponseDTO(rate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTO, "Tarifa por modalidad creada con éxito"));
    }

    /**
     * Update modality rate
     */
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<ModalityRateResponseDTO>> updateRate(
            @PathVariable UUID uuid,
            @Valid @RequestBody ModalityRateRequestDTO requestDTO) {
        ModalityRateEntity rate = modalityRateService.updateRate(
                uuid,
                requestDTO.getRatePerHour(),
                requestDTO.getEffectiveFrom(),
                requestDTO.getEffectiveTo()
        );
        ModalityRateResponseDTO responseDTO = modalityRateMapper.toResponseDTO(rate);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tarifa por modalidad actualizada con éxito")
        );
    }

    /**
     * Close a rate by setting its effectiveTo date to today
     */
    @PatchMapping("/{uuid}/close")
    public ResponseEntity<ApiResponse<ModalityRateResponseDTO>> closeRate(@PathVariable UUID uuid) {
        ModalityRateEntity rate = modalityRateService.closeRate(uuid);
        ModalityRateResponseDTO responseDTO = modalityRateMapper.toResponseDTO(rate);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Tarifa por modalidad cerrada con éxito")
        );
    }

    /**
     * Create a new rate version (closes previous and creates new)
     */
    @PostMapping("/modality/{modalityUuid}/activity-type/{activityTypeUuid}/new-version")
    public ResponseEntity<ApiResponse<ModalityRateResponseDTO>> createNewRateVersion(
            @PathVariable UUID modalityUuid,
            @PathVariable UUID activityTypeUuid,
            @RequestParam BigDecimal newRatePerHour,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom) {
        ModalityRateEntity rate = modalityRateService.createNewRateVersion(
                modalityUuid, activityTypeUuid, newRatePerHour, effectiveFrom);
        ModalityRateResponseDTO responseDTO = modalityRateMapper.toResponseDTO(rate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTO, "Nueva versión de tarifa por modalidad creada con éxito"));
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
