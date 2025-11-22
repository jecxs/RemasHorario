package com.pontificia.remashorario.modules.payrollPeriod;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.payrollPeriod.dto.PayrollPeriodRequestDTO;
import com.pontificia.remashorario.modules.payrollPeriod.dto.PayrollPeriodResponseDTO;
import com.pontificia.remashorario.modules.payrollPeriod.mapper.PayrollPeriodMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing payroll periods (weekly, biweekly, monthly)
 * Controls the lifecycle of payroll calculation periods
 */
@RestController
@RequestMapping("/api/protected/payroll-periods")
@RequiredArgsConstructor
public class PayrollPeriodController {

    private final PayrollPeriodService payrollPeriodService;
    private final PayrollPeriodMapper payrollPeriodMapper;

    /**
     * Get all payroll periods (ordered by start date descending)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PayrollPeriodResponseDTO>>> getAllPeriods() {
        List<PayrollPeriodEntity> periods = payrollPeriodService.getAllPeriods();
        List<PayrollPeriodResponseDTO> responseDTOs = payrollPeriodMapper.toResponseDTOList(periods);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Períodos de nómina recuperados con éxito")
        );
    }

    /**
     * Get payroll period by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<PayrollPeriodResponseDTO>> getPeriodById(@PathVariable UUID uuid) {
        PayrollPeriodEntity period = payrollPeriodService.getPeriodById(uuid);
        PayrollPeriodResponseDTO responseDTO = payrollPeriodMapper.toResponseDTO(period);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Período de nómina recuperado con éxito")
        );
    }

    /**
     * Get periods by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<PayrollPeriodResponseDTO>>> getPeriodsByStatus(
            @PathVariable PayrollPeriodEntity.PayrollStatus status) {
        List<PayrollPeriodEntity> periods = payrollPeriodService.getPeriodsByStatus(status);
        List<PayrollPeriodResponseDTO> responseDTOs = payrollPeriodMapper.toResponseDTOList(periods);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Períodos de nómina por estado recuperados con éxito")
        );
    }

    /**
     * Get period by specific date
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<PayrollPeriodResponseDTO>> getPeriodByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        PayrollPeriodEntity period = payrollPeriodService.getPeriodByDate(date);
        PayrollPeriodResponseDTO responseDTO = payrollPeriodMapper.toResponseDTO(period);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Período de nómina para la fecha recuperado con éxito")
        );
    }

    /**
     * Get pending periods (DRAFT or CALCULATED)
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PayrollPeriodResponseDTO>>> getPendingPeriods() {
        List<PayrollPeriodEntity> periods = payrollPeriodService.getPendingPeriods();
        List<PayrollPeriodResponseDTO> responseDTOs = payrollPeriodMapper.toResponseDTOList(periods);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Períodos de nómina pendientes recuperados con éxito")
        );
    }

    /**
     * Get past periods
     */
    @GetMapping("/past")
    public ResponseEntity<ApiResponse<List<PayrollPeriodResponseDTO>>> getPastPeriods(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate referenceDate = date != null ? date : LocalDate.now();
        List<PayrollPeriodEntity> periods = payrollPeriodService.getPastPeriods(referenceDate);
        List<PayrollPeriodResponseDTO> responseDTOs = payrollPeriodMapper.toResponseDTOList(periods);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Períodos de nómina pasados recuperados con éxito")
        );
    }

    /**
     * Get future periods
     */
    @GetMapping("/future")
    public ResponseEntity<ApiResponse<List<PayrollPeriodResponseDTO>>> getFuturePeriods(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate referenceDate = date != null ? date : LocalDate.now();
        List<PayrollPeriodEntity> periods = payrollPeriodService.getFuturePeriods(referenceDate);
        List<PayrollPeriodResponseDTO> responseDTOs = payrollPeriodMapper.toResponseDTOList(periods);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Períodos de nómina futuros recuperados con éxito")
        );
    }

    /**
     * Create new payroll period
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PayrollPeriodResponseDTO>> createPeriod(
            @Valid @RequestBody PayrollPeriodRequestDTO requestDTO) {
        PayrollPeriodEntity period = payrollPeriodService.createPeriod(
                requestDTO.getName(),
                requestDTO.getStartDate(),
                requestDTO.getEndDate()
        );
        PayrollPeriodResponseDTO responseDTO = payrollPeriodMapper.toResponseDTO(period);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTO, "Período de nómina creado con éxito"));
    }

    /**
     * Update payroll period (only if status is DRAFT)
     */
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<PayrollPeriodResponseDTO>> updatePeriod(
            @PathVariable UUID uuid,
            @Valid @RequestBody PayrollPeriodRequestDTO requestDTO) {
        PayrollPeriodEntity period = payrollPeriodService.updatePeriod(
                uuid,
                requestDTO.getName(),
                requestDTO.getStartDate(),
                requestDTO.getEndDate()
        );
        PayrollPeriodResponseDTO responseDTO = payrollPeriodMapper.toResponseDTO(period);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Período de nómina actualizado con éxito")
        );
    }

    /**
     * Delete payroll period (only if status is DRAFT)
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deletePeriod(@PathVariable UUID uuid) {
        payrollPeriodService.deletePeriod(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Período de nómina eliminado con éxito")
        );
    }

    /**
     * Mark period as CALCULATED
     */
    @PatchMapping("/{uuid}/mark-calculated")
    public ResponseEntity<ApiResponse<PayrollPeriodResponseDTO>> markAsCalculated(@PathVariable UUID uuid) {
        PayrollPeriodEntity period = payrollPeriodService.markAsCalculated(uuid);
        PayrollPeriodResponseDTO responseDTO = payrollPeriodMapper.toResponseDTO(period);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Período de nómina marcado como calculado con éxito")
        );
    }

    /**
     * Mark period as APPROVED
     */
    @PatchMapping("/{uuid}/mark-approved")
    public ResponseEntity<ApiResponse<PayrollPeriodResponseDTO>> markAsApproved(@PathVariable UUID uuid) {
        PayrollPeriodEntity period = payrollPeriodService.markAsApproved(uuid);
        PayrollPeriodResponseDTO responseDTO = payrollPeriodMapper.toResponseDTO(period);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Período de nómina aprobado con éxito")
        );
    }

    /**
     * Mark period as PAID
     */
    @PatchMapping("/{uuid}/mark-paid")
    public ResponseEntity<ApiResponse<PayrollPeriodResponseDTO>> markAsPaid(@PathVariable UUID uuid) {
        PayrollPeriodEntity period = payrollPeriodService.markAsPaid(uuid);
        PayrollPeriodResponseDTO responseDTO = payrollPeriodMapper.toResponseDTO(period);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Período de nómina marcado como pagado con éxito")
        );
    }

    /**
     * Revert period back to DRAFT status
     */
    @PatchMapping("/{uuid}/revert-to-draft")
    public ResponseEntity<ApiResponse<PayrollPeriodResponseDTO>> revertToDraft(@PathVariable UUID uuid) {
        PayrollPeriodEntity period = payrollPeriodService.revertToDraft(uuid);
        PayrollPeriodResponseDTO responseDTO = payrollPeriodMapper.toResponseDTO(period);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Período de nómina revertido a borrador con éxito")
        );
    }

    /**
     * Create weekly periods for a month
     */
    @PostMapping("/generate/weekly")
    public ResponseEntity<ApiResponse<List<PayrollPeriodResponseDTO>>> createWeeklyPeriodsForMonth(
            @RequestParam int year,
            @RequestParam int month) {
        List<PayrollPeriodEntity> periods = payrollPeriodService.createWeeklyPeriodsForMonth(year, month);
        List<PayrollPeriodResponseDTO> responseDTOs = payrollPeriodMapper.toResponseDTOList(periods);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTOs, "Períodos semanales creados con éxito"));
    }

    /**
     * Create biweekly periods for a month
     */
    @PostMapping("/generate/biweekly")
    public ResponseEntity<ApiResponse<List<PayrollPeriodResponseDTO>>> createBiweeklyPeriodsForMonth(
            @RequestParam int year,
            @RequestParam int month) {
        List<PayrollPeriodEntity> periods = payrollPeriodService.createBiweeklyPeriodsForMonth(year, month);
        List<PayrollPeriodResponseDTO> responseDTOs = payrollPeriodMapper.toResponseDTOList(periods);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTOs, "Períodos quincenales creados con éxito"));
    }

    /**
     * Create monthly period
     */
    @PostMapping("/generate/monthly")
    public ResponseEntity<ApiResponse<PayrollPeriodResponseDTO>> createMonthlyPeriod(
            @RequestParam int year,
            @RequestParam int month) {
        PayrollPeriodEntity period = payrollPeriodService.createMonthlyPeriod(year, month);
        PayrollPeriodResponseDTO responseDTO = payrollPeriodMapper.toResponseDTO(period);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTO, "Período mensual creado con éxito"));
    }

    /**
     * Check if a period can be modified
     */
    @GetMapping("/{uuid}/can-modify")
    public ResponseEntity<ApiResponse<Boolean>> canModify(@PathVariable UUID uuid) {
        boolean canModify = payrollPeriodService.canModify(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(canModify, "Verificación de modificación realizada con éxito")
        );
    }

    /**
     * Check if a period can be deleted
     */
    @GetMapping("/{uuid}/can-delete")
    public ResponseEntity<ApiResponse<Boolean>> canDelete(@PathVariable UUID uuid) {
        boolean canDelete = payrollPeriodService.canDelete(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(canDelete, "Verificación de eliminación realizada con éxito")
        );
    }
}
