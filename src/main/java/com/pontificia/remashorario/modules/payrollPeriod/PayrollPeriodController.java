package com.pontificia.remashorario.modules.payrollPeriod;

import com.pontificia.remashorario.config.ApiResponse;
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

    /**
     * Get all payroll periods (ordered by start date descending)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PayrollPeriodEntity>>> getAllPeriods() {
        List<PayrollPeriodEntity> periods = payrollPeriodService.getAllPeriods();
        return ResponseEntity.ok(
                ApiResponse.success(periods, "Períodos de nómina recuperados con éxito")
        );
    }

    /**
     * Get payroll period by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<PayrollPeriodEntity>> getPeriodById(@PathVariable UUID uuid) {
        PayrollPeriodEntity period = payrollPeriodService.getPeriodById(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(period, "Período de nómina recuperado con éxito")
        );
    }

    /**
     * Get periods by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<PayrollPeriodEntity>>> getPeriodsByStatus(
            @PathVariable PayrollPeriodEntity.PayrollStatus status) {
        List<PayrollPeriodEntity> periods = payrollPeriodService.getPeriodsByStatus(status);
        return ResponseEntity.ok(
                ApiResponse.success(periods, "Períodos de nómina por estado recuperados con éxito")
        );
    }

    /**
     * Get period by specific date
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<PayrollPeriodEntity>> getPeriodByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        PayrollPeriodEntity period = payrollPeriodService.getPeriodByDate(date);
        return ResponseEntity.ok(
                ApiResponse.success(period, "Período de nómina para la fecha recuperado con éxito")
        );
    }

    /**
     * Get pending periods (DRAFT or CALCULATED)
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PayrollPeriodEntity>>> getPendingPeriods() {
        List<PayrollPeriodEntity> periods = payrollPeriodService.getPendingPeriods();
        return ResponseEntity.ok(
                ApiResponse.success(periods, "Períodos de nómina pendientes recuperados con éxito")
        );
    }

    /**
     * Get past periods
     */
    @GetMapping("/past")
    public ResponseEntity<ApiResponse<List<PayrollPeriodEntity>>> getPastPeriods(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate referenceDate = date != null ? date : LocalDate.now();
        List<PayrollPeriodEntity> periods = payrollPeriodService.getPastPeriods(referenceDate);
        return ResponseEntity.ok(
                ApiResponse.success(periods, "Períodos de nómina pasados recuperados con éxito")
        );
    }

    /**
     * Get future periods
     */
    @GetMapping("/future")
    public ResponseEntity<ApiResponse<List<PayrollPeriodEntity>>> getFuturePeriods(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate referenceDate = date != null ? date : LocalDate.now();
        List<PayrollPeriodEntity> periods = payrollPeriodService.getFuturePeriods(referenceDate);
        return ResponseEntity.ok(
                ApiResponse.success(periods, "Períodos de nómina futuros recuperados con éxito")
        );
    }

    /**
     * Create new payroll period
     * TODO: Replace parameters with PayrollPeriodRequestDTO when DTOs are created
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PayrollPeriodEntity>> createPeriod(
            @RequestParam String name,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        PayrollPeriodEntity period = payrollPeriodService.createPeriod(name, startDate, endDate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(period, "Período de nómina creado con éxito"));
    }

    /**
     * Update payroll period (only if status is DRAFT)
     * TODO: Replace parameters with PayrollPeriodRequestDTO when DTOs are created
     */
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<PayrollPeriodEntity>> updatePeriod(
            @PathVariable UUID uuid,
            @RequestParam String name,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        PayrollPeriodEntity period = payrollPeriodService.updatePeriod(uuid, name, startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success(period, "Período de nómina actualizado con éxito")
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
    public ResponseEntity<ApiResponse<PayrollPeriodEntity>> markAsCalculated(@PathVariable UUID uuid) {
        PayrollPeriodEntity period = payrollPeriodService.markAsCalculated(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(period, "Período de nómina marcado como calculado con éxito")
        );
    }

    /**
     * Mark period as APPROVED
     */
    @PatchMapping("/{uuid}/mark-approved")
    public ResponseEntity<ApiResponse<PayrollPeriodEntity>> markAsApproved(@PathVariable UUID uuid) {
        PayrollPeriodEntity period = payrollPeriodService.markAsApproved(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(period, "Período de nómina aprobado con éxito")
        );
    }

    /**
     * Mark period as PAID
     */
    @PatchMapping("/{uuid}/mark-paid")
    public ResponseEntity<ApiResponse<PayrollPeriodEntity>> markAsPaid(@PathVariable UUID uuid) {
        PayrollPeriodEntity period = payrollPeriodService.markAsPaid(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(period, "Período de nómina marcado como pagado con éxito")
        );
    }

    /**
     * Revert period back to DRAFT status
     */
    @PatchMapping("/{uuid}/revert-to-draft")
    public ResponseEntity<ApiResponse<PayrollPeriodEntity>> revertToDraft(@PathVariable UUID uuid) {
        PayrollPeriodEntity period = payrollPeriodService.revertToDraft(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(period, "Período de nómina revertido a borrador con éxito")
        );
    }

    /**
     * Create weekly periods for a month
     */
    @PostMapping("/generate/weekly")
    public ResponseEntity<ApiResponse<List<PayrollPeriodEntity>>> createWeeklyPeriodsForMonth(
            @RequestParam int year,
            @RequestParam int month) {
        List<PayrollPeriodEntity> periods = payrollPeriodService.createWeeklyPeriodsForMonth(year, month);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(periods, "Períodos semanales creados con éxito"));
    }

    /**
     * Create biweekly periods for a month
     */
    @PostMapping("/generate/biweekly")
    public ResponseEntity<ApiResponse<List<PayrollPeriodEntity>>> createBiweeklyPeriodsForMonth(
            @RequestParam int year,
            @RequestParam int month) {
        List<PayrollPeriodEntity> periods = payrollPeriodService.createBiweeklyPeriodsForMonth(year, month);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(periods, "Períodos quincenales creados con éxito"));
    }

    /**
     * Create monthly period
     */
    @PostMapping("/generate/monthly")
    public ResponseEntity<ApiResponse<PayrollPeriodEntity>> createMonthlyPeriod(
            @RequestParam int year,
            @RequestParam int month) {
        PayrollPeriodEntity period = payrollPeriodService.createMonthlyPeriod(year, month);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(period, "Período mensual creado con éxito"));
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
