package com.pontificia.remashorario.modules.payrollLine;

import com.pontificia.remashorario.config.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for calculating and managing teacher payroll
 * Handles complex payroll calculations including attendance, extra assignments, and penalties
 */
@RestController
@RequestMapping("/api/protected/payroll-lines")
@RequiredArgsConstructor
public class PayrollLineController {

    private final PayrollLineService payrollLineService;

    /**
     * Get all payroll lines
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PayrollLineEntity>>> getAllPayrollLines() {
        List<PayrollLineEntity> payrollLines = payrollLineService.getAllPayrollLines();
        return ResponseEntity.ok(
                ApiResponse.success(payrollLines, "Líneas de nómina recuperadas con éxito")
        );
    }

    /**
     * Get payroll line by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<PayrollLineEntity>> getPayrollLineById(@PathVariable UUID uuid) {
        PayrollLineEntity payrollLine = payrollLineService.getPayrollLineById(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(payrollLine, "Línea de nómina recuperada con éxito")
        );
    }

    /**
     * Get payroll line by ID with full details
     */
    @GetMapping("/{uuid}/details")
    public ResponseEntity<ApiResponse<PayrollLineEntity>> getPayrollLineByIdWithDetails(@PathVariable UUID uuid) {
        PayrollLineEntity payrollLine = payrollLineService.getPayrollLineByIdWithDetails(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(payrollLine, "Línea de nómina con detalles recuperada con éxito")
        );
    }

    /**
     * Get all payroll lines for a specific period
     */
    @GetMapping("/period/{payrollPeriodUuid}")
    public ResponseEntity<ApiResponse<List<PayrollLineEntity>>> getPayrollLinesByPeriod(
            @PathVariable UUID payrollPeriodUuid) {
        List<PayrollLineEntity> payrollLines = payrollLineService.getPayrollLinesByPeriod(payrollPeriodUuid);
        return ResponseEntity.ok(
                ApiResponse.success(payrollLines, "Líneas de nómina del período recuperadas con éxito")
        );
    }

    /**
     * Get all payroll lines for a specific teacher (ordered by period descending)
     */
    @GetMapping("/teacher/{teacherUuid}")
    public ResponseEntity<ApiResponse<List<PayrollLineEntity>>> getPayrollLinesByTeacher(
            @PathVariable UUID teacherUuid) {
        List<PayrollLineEntity> payrollLines = payrollLineService.getPayrollLinesByTeacher(teacherUuid);
        return ResponseEntity.ok(
                ApiResponse.success(payrollLines, "Líneas de nómina del docente recuperadas con éxito")
        );
    }

    /**
     * Get payroll line for a specific teacher in a specific period
     */
    @GetMapping("/period/{payrollPeriodUuid}/teacher/{teacherUuid}")
    public ResponseEntity<ApiResponse<PayrollLineEntity>> getPayrollLineByPeriodAndTeacher(
            @PathVariable UUID payrollPeriodUuid,
            @PathVariable UUID teacherUuid) {
        PayrollLineEntity payrollLine = payrollLineService.getPayrollLineByPeriodAndTeacher(
                payrollPeriodUuid, teacherUuid);
        return ResponseEntity.ok(
                ApiResponse.success(payrollLine, "Línea de nómina del docente en el período recuperada con éxito")
        );
    }

    /**
     * Calculate payroll for a specific teacher in a period
     * This is the main calculation endpoint
     */
    @PostMapping("/calculate/period/{payrollPeriodUuid}/teacher/{teacherUuid}")
    public ResponseEntity<ApiResponse<PayrollLineEntity>> calculatePayrollForTeacher(
            @PathVariable UUID payrollPeriodUuid,
            @PathVariable UUID teacherUuid) {
        PayrollLineEntity payrollLine = payrollLineService.calculatePayrollForTeacher(payrollPeriodUuid, teacherUuid);
        return ResponseEntity.ok(
                ApiResponse.success(payrollLine, "Nómina del docente calculada con éxito")
        );
    }

    /**
     * Calculate payroll for all teachers in a period
     * This endpoint triggers calculation for all teachers who have attendance or extra assignments
     */
    @PostMapping("/calculate/period/{payrollPeriodUuid}")
    public ResponseEntity<ApiResponse<List<PayrollLineEntity>>> calculatePayrollForAllTeachers(
            @PathVariable UUID payrollPeriodUuid) {
        List<PayrollLineEntity> payrollLines = payrollLineService.calculatePayrollForAllTeachers(payrollPeriodUuid);
        return ResponseEntity.ok(
                ApiResponse.success(payrollLines, "Nómina de todos los docentes calculada con éxito")
        );
    }

    /**
     * Recalculate all payroll lines for a period
     * Deletes existing calculations and recalculates from scratch
     */
    @PostMapping("/recalculate/period/{payrollPeriodUuid}")
    public ResponseEntity<ApiResponse<List<PayrollLineEntity>>> recalculatePayrollForPeriod(
            @PathVariable UUID payrollPeriodUuid) {
        List<PayrollLineEntity> payrollLines = payrollLineService.recalculatePayrollForPeriod(payrollPeriodUuid);
        return ResponseEntity.ok(
                ApiResponse.success(payrollLines, "Nómina del período recalculada con éxito")
        );
    }

    /**
     * Get total net amount for a period
     */
    @GetMapping("/period/{payrollPeriodUuid}/total-net-amount")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalNetAmountByPeriod(
            @PathVariable UUID payrollPeriodUuid) {
        BigDecimal totalNetAmount = payrollLineService.getTotalNetAmountByPeriod(payrollPeriodUuid);
        return ResponseEntity.ok(
                ApiResponse.success(totalNetAmount, "Total neto del período calculado con éxito")
        );
    }

    /**
     * Get total penalties for a period
     */
    @GetMapping("/period/{payrollPeriodUuid}/total-penalties")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalPenaltiesByPeriod(
            @PathVariable UUID payrollPeriodUuid) {
        BigDecimal totalPenalties = payrollLineService.getTotalPenaltiesByPeriod(payrollPeriodUuid);
        return ResponseEntity.ok(
                ApiResponse.success(totalPenalties, "Total de penalizaciones del período calculado con éxito")
        );
    }

    /**
     * Get teacher count for a period
     */
    @GetMapping("/period/{payrollPeriodUuid}/teacher-count")
    public ResponseEntity<ApiResponse<Long>> getTeacherCountByPeriod(
            @PathVariable UUID payrollPeriodUuid) {
        Long teacherCount = payrollLineService.getTeacherCountByPeriod(payrollPeriodUuid);
        return ResponseEntity.ok(
                ApiResponse.success(teacherCount, "Cantidad de docentes del período calculada con éxito")
        );
    }

    /**
     * Delete a payroll line (only if period is in DRAFT status)
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deletePayrollLine(@PathVariable UUID uuid) {
        payrollLineService.deletePayrollLine(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Línea de nómina eliminada con éxito")
        );
    }

    /**
     * Get period summary (aggregated statistics)
     */
    @GetMapping("/period/{payrollPeriodUuid}/summary")
    public ResponseEntity<ApiResponse<PayrollPeriodSummary>> getPeriodSummary(
            @PathVariable UUID payrollPeriodUuid) {
        BigDecimal totalNet = payrollLineService.getTotalNetAmountByPeriod(payrollPeriodUuid);
        BigDecimal totalPenalties = payrollLineService.getTotalPenaltiesByPeriod(payrollPeriodUuid);
        Long teacherCount = payrollLineService.getTeacherCountByPeriod(payrollPeriodUuid);

        PayrollPeriodSummary summary = new PayrollPeriodSummary(
                totalNet,
                totalPenalties,
                totalNet.add(totalPenalties), // gross amount
                teacherCount
        );

        return ResponseEntity.ok(
                ApiResponse.success(summary, "Resumen del período calculado con éxito")
        );
    }

    /**
     * Inner class for period summary response
     */
    public static class PayrollPeriodSummary {
        public final BigDecimal totalNetAmount;
        public final BigDecimal totalPenalties;
        public final BigDecimal totalGrossAmount;
        public final Long teacherCount;

        public PayrollPeriodSummary(BigDecimal totalNetAmount, BigDecimal totalPenalties,
                                  BigDecimal totalGrossAmount, Long teacherCount) {
            this.totalNetAmount = totalNetAmount;
            this.totalPenalties = totalPenalties;
            this.totalGrossAmount = totalGrossAmount;
            this.teacherCount = teacherCount;
        }
    }
}
