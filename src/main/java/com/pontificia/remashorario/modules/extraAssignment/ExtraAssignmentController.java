package com.pontificia.remashorario.modules.extraAssignment;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.extraAssignment.dto.ExtraAssignmentRequestDTO;
import com.pontificia.remashorario.modules.extraAssignment.dto.ExtraAssignmentResponseDTO;
import com.pontificia.remashorario.modules.extraAssignment.mapper.ExtraAssignmentMapper;
import jakarta.validation.Valid;
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
import java.util.stream.Collectors;

/**
 * REST Controller for managing extra assignments (workshops, substitute exams, etc.)
 * These are activities outside the regular schedule
 */
@RestController
@RequestMapping("/api/protected/extra-assignments")
@RequiredArgsConstructor
public class ExtraAssignmentController {

    private final ExtraAssignmentService extraAssignmentService;
    private final ExtraAssignmentMapper extraAssignmentMapper;

    /**
     * Get all extra assignments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExtraAssignmentResponseDTO>>> getAllAssignments() {
        List<ExtraAssignmentEntity> assignments = extraAssignmentService.getAllAssignments();
        List<ExtraAssignmentResponseDTO> responseDTOs = extraAssignmentMapper.toResponseDTOList(assignments);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Asignaciones extra recuperadas con éxito")
        );
    }

    /**
     * Get extra assignment by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<ExtraAssignmentResponseDTO>> getAssignmentById(@PathVariable UUID uuid) {
        ExtraAssignmentEntity assignment = extraAssignmentService.getAssignmentById(uuid);
        ExtraAssignmentResponseDTO responseDTO = extraAssignmentMapper.toResponseDTO(assignment);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Asignación extra recuperada con éxito")
        );
    }

    /**
     * Get extra assignment by ID with full details
     */
    @GetMapping("/{uuid}/details")
    public ResponseEntity<ApiResponse<ExtraAssignmentResponseDTO>> getAssignmentByIdWithDetails(@PathVariable UUID uuid) {
        ExtraAssignmentEntity assignment = extraAssignmentService.getAssignmentByIdWithDetails(uuid);
        ExtraAssignmentResponseDTO responseDTO = extraAssignmentMapper.toResponseDTO(assignment);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Asignación extra con detalles recuperada con éxito")
        );
    }

    /**
     * Get all assignments for a specific teacher
     */
    @GetMapping("/teacher/{teacherUuid}")
    public ResponseEntity<ApiResponse<List<ExtraAssignmentResponseDTO>>> getAssignmentsByTeacher(
            @PathVariable UUID teacherUuid) {
        List<ExtraAssignmentEntity> assignments = extraAssignmentService.getAssignmentsByTeacher(teacherUuid);
        List<ExtraAssignmentResponseDTO> responseDTOs = extraAssignmentMapper.toResponseDTOList(assignments);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Asignaciones extra del docente recuperadas con éxito")
        );
    }

    /**
     * Get assignments for a teacher on a specific date
     */
    @GetMapping("/teacher/{teacherUuid}/date/{date}")
    public ResponseEntity<ApiResponse<List<ExtraAssignmentResponseDTO>>> getAssignmentsByTeacherAndDate(
            @PathVariable UUID teacherUuid,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ExtraAssignmentEntity> assignments = extraAssignmentService.getAssignmentsByTeacherAndDate(teacherUuid, date);
        List<ExtraAssignmentResponseDTO> responseDTOs = extraAssignmentMapper.toResponseDTOList(assignments);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Asignaciones extra del docente en la fecha recuperadas con éxito")
        );
    }

    /**
     * Get assignments for a teacher in a date range
     */
    @GetMapping("/teacher/{teacherUuid}/range")
    public ResponseEntity<ApiResponse<List<ExtraAssignmentResponseDTO>>> getAssignmentsByTeacherAndDateRange(
            @PathVariable UUID teacherUuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ExtraAssignmentEntity> assignments = extraAssignmentService.getAssignmentsByTeacherAndDateRange(
                teacherUuid, startDate, endDate);
        List<ExtraAssignmentResponseDTO> responseDTOs = extraAssignmentMapper.toResponseDTOList(assignments);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Asignaciones extra del docente en el rango recuperadas con éxito")
        );
    }

    /**
     * Get assignments in a date range
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<ExtraAssignmentResponseDTO>>> getAssignmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ExtraAssignmentEntity> assignments = extraAssignmentService.getAssignmentsByDateRange(startDate, endDate);
        List<ExtraAssignmentResponseDTO> responseDTOs = extraAssignmentMapper.toResponseDTOList(assignments);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Asignaciones extra en el rango recuperadas con éxito")
        );
    }

    /**
     * Get assignments by activity type
     */
    @GetMapping("/activity-type/{activityTypeUuid}")
    public ResponseEntity<ApiResponse<List<ExtraAssignmentResponseDTO>>> getAssignmentsByActivityType(
            @PathVariable UUID activityTypeUuid) {
        List<ExtraAssignmentEntity> assignments = extraAssignmentService.getAssignmentsByActivityType(activityTypeUuid);
        List<ExtraAssignmentResponseDTO> responseDTOs = extraAssignmentMapper.toResponseDTOList(assignments);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Asignaciones extra del tipo de actividad recuperadas con éxito")
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
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ExtraAssignmentResponseDTO>> createAssignment(
            @Valid @RequestBody ExtraAssignmentRequestDTO requestDTO) {
        ExtraAssignmentEntity assignment = extraAssignmentService.createAssignment(
                requestDTO.getTeacherUuid(),
                requestDTO.getActivityTypeUuid(),
                requestDTO.getTitle(),
                requestDTO.getAssignmentDate(),
                requestDTO.getStartTime(),
                requestDTO.getEndTime(),
                requestDTO.getRatePerHour(),
                requestDTO.getNotes()
        );
        ExtraAssignmentResponseDTO responseDTO = extraAssignmentMapper.toResponseDTO(assignment);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTO, "Asignación extra creada con éxito"));
    }

    /**
     * Update extra assignment
     */
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<ExtraAssignmentResponseDTO>> updateAssignment(
            @PathVariable UUID uuid,
            @Valid @RequestBody ExtraAssignmentRequestDTO requestDTO) {
        ExtraAssignmentEntity assignment = extraAssignmentService.updateAssignment(
                uuid,
                requestDTO.getTitle(),
                requestDTO.getAssignmentDate(),
                requestDTO.getStartTime(),
                requestDTO.getEndTime(),
                requestDTO.getRatePerHour(),
                requestDTO.getNotes()
        );
        ExtraAssignmentResponseDTO responseDTO = extraAssignmentMapper.toResponseDTO(assignment);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Asignación extra actualizada con éxito")
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
     */
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<ExtraAssignmentResponseDTO>>> createBulkAssignments(
            @Valid @RequestBody List<ExtraAssignmentRequestDTO> assignmentRequests) {
        List<ExtraAssignmentEntity> entitiesToCreate = assignmentRequests.stream()
                .map(dto -> {
                    ExtraAssignmentEntity entity = new ExtraAssignmentEntity();
                    entity.setTitle(dto.getTitle());
                    entity.setAssignmentDate(dto.getAssignmentDate());
                    entity.setStartTime(dto.getStartTime());
                    entity.setEndTime(dto.getEndTime());
                    entity.setRatePerHour(dto.getRatePerHour());
                    entity.setNotes(dto.getNotes());
                    // teacherUuid and activityTypeUuid will be handled by service
                    return entity;
                })
                .collect(Collectors.toList());

        List<ExtraAssignmentEntity> created = extraAssignmentService.createBulkAssignments(entitiesToCreate);
        List<ExtraAssignmentResponseDTO> responseDTOs = extraAssignmentMapper.toResponseDTOList(created);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTOs, "Asignaciones extra creadas en masa con éxito"));
    }
}
