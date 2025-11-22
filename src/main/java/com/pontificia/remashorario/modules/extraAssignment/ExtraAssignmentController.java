package com.pontificia.remashorario.modules.extraAssignment;

import com.pontificia.remashorario.config.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing extra assignments (workshops, substitute exams, etc.)
 * These are activities outside the regular schedule
 */
@RestController
@RequestMapping("/api/protected/extra-assignments")
@RequiredArgsConstructor
public class ExtraAssignmentController {

    private final ExtraAssignmentService extraAssignmentService;

    /**
     * Get all extra assignments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExtraAssignmentEntity>>> getAllAssignments() {
        List<ExtraAssignmentEntity> assignments = extraAssignmentService.getAllAssignments();
        return ResponseEntity.ok(
                ApiResponse.success(assignments, "Asignaciones extra recuperadas con éxito")
        );
    }

    /**
     * Get extra assignment by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<ExtraAssignmentEntity>> getAssignmentById(@PathVariable UUID uuid) {
        ExtraAssignmentEntity assignment = extraAssignmentService.getAssignmentById(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(assignment, "Asignación extra recuperada con éxito")
        );
    }

    /**
     * Get extra assignment by ID with full details
     */
    @GetMapping("/{uuid}/details")
    public ResponseEntity<ApiResponse<ExtraAssignmentEntity>> getAssignmentByIdWithDetails(@PathVariable UUID uuid) {
        ExtraAssignmentEntity assignment = extraAssignmentService.getAssignmentByIdWithDetails(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(assignment, "Asignación extra con detalles recuperada con éxito")
        );
    }

    /**
     * Get all assignments for a specific teacher
     */
    @GetMapping("/teacher/{teacherUuid}")
    public ResponseEntity<ApiResponse<List<ExtraAssignmentEntity>>> getAssignmentsByTeacher(
            @PathVariable UUID teacherUuid) {
        List<ExtraAssignmentEntity> assignments = extraAssignmentService.getAssignmentsByTeacher(teacherUuid);
        return ResponseEntity.ok(
                ApiResponse.success(assignments, "Asignaciones extra del docente recuperadas con éxito")
        );
    }

    /**
     * Get assignments for a teacher on a specific date
     */
    @GetMapping("/teacher/{teacherUuid}/date/{date}")
    public ResponseEntity<ApiResponse<List<ExtraAssignmentEntity>>> getAssignmentsByTeacherAndDate(
            @PathVariable UUID teacherUuid,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ExtraAssignmentEntity> assignments = extraAssignmentService.getAssignmentsByTeacherAndDate(teacherUuid, date);
        return ResponseEntity.ok(
                ApiResponse.success(assignments, "Asignaciones extra del docente en la fecha recuperadas con éxito")
        );
    }

    /**
     * Get assignments for a teacher in a date range
     */
    @GetMapping("/teacher/{teacherUuid}/range")
    public ResponseEntity<ApiResponse<List<ExtraAssignmentEntity>>> getAssignmentsByTeacherAndDateRange(
            @PathVariable UUID teacherUuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ExtraAssignmentEntity> assignments = extraAssignmentService.getAssignmentsByTeacherAndDateRange(
                teacherUuid, startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success(assignments, "Asignaciones extra del docente en el rango recuperadas con éxito")
        );
    }

    /**
     * Get assignments in a date range
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<ExtraAssignmentEntity>>> getAssignmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ExtraAssignmentEntity> assignments = extraAssignmentService.getAssignmentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success(assignments, "Asignaciones extra en el rango recuperadas con éxito")
        );
    }

    /**
     * Get assignments by activity type
     */
    @GetMapping("/activity-type/{activityTypeUuid}")
    public ResponseEntity<ApiResponse<List<ExtraAssignmentEntity>>> getAssignmentsByActivityType(
            @PathVariable UUID activityTypeUuid) {
        List<ExtraAssignmentEntity> assignments = extraAssignmentService.getAssignmentsByActivityType(activityTypeUuid);
        return ResponseEntity.ok(
                ApiResponse.success(assignments, "Asignaciones extra del tipo de actividad recuperadas con éxito")
        );
    }

    /**
     * Calculate payment for a specific assignment
     */
    @GetMapping("/{uuid}/calculate-payment")
    public ResponseEntity<ApiResponse<BigDecimal>> calculatePayment(@PathVariable UUID uuid) {
        BigDecimal payment = extraAssignmentService.calculatePayment(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(payment, "Pago de asignación extra calculado con éxito")
        );
    }

    /**
     * Get total hours for a teacher in a date range
     */
    @GetMapping("/teacher/{teacherUuid}/total-hours")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalHoursForTeacher(
            @PathVariable UUID teacherUuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal totalHours = extraAssignmentService.getTotalHoursForTeacher(teacherUuid, startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success(totalHours, "Total de horas extra calculado con éxito")
        );
    }

    /**
     * Get total payment for a teacher in a date range
     */
    @GetMapping("/teacher/{teacherUuid}/total-payment")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalPaymentForTeacher(
            @PathVariable UUID teacherUuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal totalPayment = extraAssignmentService.getTotalPaymentForTeacher(teacherUuid, startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success(totalPayment, "Total de pago extra calculado con éxito")
        );
    }

    /**
     * Create new extra assignment
     * TODO: Replace parameters with ExtraAssignmentRequestDTO when DTOs are created
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ExtraAssignmentEntity>> createAssignment(
            @RequestParam UUID teacherUuid,
            @RequestParam UUID activityTypeUuid,
            @RequestParam String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate assignmentDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) BigDecimal ratePerHour,
            @RequestParam(required = false) String notes) {
        ExtraAssignmentEntity assignment = extraAssignmentService.createAssignment(
                teacherUuid, activityTypeUuid, title, assignmentDate, startTime, endTime, ratePerHour, notes);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(assignment, "Asignación extra creada con éxito"));
    }

    /**
     * Update extra assignment
     * TODO: Replace parameters with ExtraAssignmentRequestDTO when DTOs are created
     */
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<ExtraAssignmentEntity>> updateAssignment(
            @PathVariable UUID uuid,
            @RequestParam String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate assignmentDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) BigDecimal ratePerHour,
            @RequestParam(required = false) String notes) {
        ExtraAssignmentEntity assignment = extraAssignmentService.updateAssignment(
                uuid, title, assignmentDate, startTime, endTime, ratePerHour, notes);
        return ResponseEntity.ok(
                ApiResponse.success(assignment, "Asignación extra actualizada con éxito")
        );
    }

    /**
     * Delete extra assignment
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable UUID uuid) {
        extraAssignmentService.deleteAssignment(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Asignación extra eliminada con éxito")
        );
    }

    /**
     * Bulk create assignments
     * TODO: Replace List<ExtraAssignmentEntity> with BulkExtraAssignmentRequestDTO when DTOs are created
     */
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<ExtraAssignmentEntity>>> createBulkAssignments(
            @RequestBody List<ExtraAssignmentEntity> assignments) {
        List<ExtraAssignmentEntity> created = extraAssignmentService.createBulkAssignments(assignments);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Asignaciones extra creadas en masa con éxito"));
    }
}
