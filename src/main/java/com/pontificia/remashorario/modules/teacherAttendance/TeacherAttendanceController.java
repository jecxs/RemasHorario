package com.pontificia.remashorario.modules.teacherAttendance;

import com.pontificia.remashorario.config.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing teacher attendance
 * Handles check-in/check-out, penalties, and admin overrides
 */
@RestController
@RequestMapping("/api/protected/teacher-attendances")
@RequiredArgsConstructor
public class TeacherAttendanceController {

    private final TeacherAttendanceService attendanceService;

    /**
     * Get all attendances
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TeacherAttendanceEntity>>> getAllAttendances() {
        List<TeacherAttendanceEntity> attendances = attendanceService.getAllAttendances();
        return ResponseEntity.ok(
                ApiResponse.success(attendances, "Asistencias recuperadas con éxito")
        );
    }

    /**
     * Get attendance by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<TeacherAttendanceEntity>> getAttendanceById(@PathVariable UUID uuid) {
        TeacherAttendanceEntity attendance = attendanceService.getAttendanceById(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(attendance, "Asistencia recuperada con éxito")
        );
    }

    /**
     * Get attendance by ID with full details
     */
    @GetMapping("/{uuid}/details")
    public ResponseEntity<ApiResponse<TeacherAttendanceEntity>> getAttendanceByIdWithDetails(@PathVariable UUID uuid) {
        TeacherAttendanceEntity attendance = attendanceService.getAttendanceByIdWithDetails(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(attendance, "Asistencia con detalles recuperada con éxito")
        );
    }

    /**
     * Get all attendances for a teacher
     */
    @GetMapping("/teacher/{teacherUuid}")
    public ResponseEntity<ApiResponse<List<TeacherAttendanceEntity>>> getAttendancesByTeacher(
            @PathVariable UUID teacherUuid) {
        List<TeacherAttendanceEntity> attendances = attendanceService.getAttendancesByTeacher(teacherUuid);
        return ResponseEntity.ok(
                ApiResponse.success(attendances, "Asistencias del docente recuperadas con éxito")
        );
    }

    /**
     * Get attendances for a teacher on a specific date
     */
    @GetMapping("/teacher/{teacherUuid}/date/{date}")
    public ResponseEntity<ApiResponse<List<TeacherAttendanceEntity>>> getAttendancesByTeacherAndDate(
            @PathVariable UUID teacherUuid,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TeacherAttendanceEntity> attendances = attendanceService.getAttendancesByTeacherAndDate(teacherUuid, date);
        return ResponseEntity.ok(
                ApiResponse.success(attendances, "Asistencias del docente en la fecha recuperadas con éxito")
        );
    }

    /**
     * Get attendances for a teacher in a date range
     */
    @GetMapping("/teacher/{teacherUuid}/range")
    public ResponseEntity<ApiResponse<List<TeacherAttendanceEntity>>> getAttendancesByTeacherAndDateRange(
            @PathVariable UUID teacherUuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TeacherAttendanceEntity> attendances = attendanceService.getAttendancesByTeacherAndDateRange(
                teacherUuid, startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success(attendances, "Asistencias del docente en el rango recuperadas con éxito")
        );
    }

    /**
     * Get attendances in a date range
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<TeacherAttendanceEntity>>> getAttendancesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TeacherAttendanceEntity> attendances = attendanceService.getAttendancesByDateRange(startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success(attendances, "Asistencias en el rango recuperadas con éxito")
        );
    }

    /**
     * Get pending attendances for a teacher
     */
    @GetMapping("/teacher/{teacherUuid}/pending")
    public ResponseEntity<ApiResponse<List<TeacherAttendanceEntity>>> getPendingAttendancesByTeacher(
            @PathVariable UUID teacherUuid) {
        List<TeacherAttendanceEntity> attendances = attendanceService.getPendingAttendancesByTeacher(teacherUuid);
        return ResponseEntity.ok(
                ApiResponse.success(attendances, "Asistencias pendientes del docente recuperadas con éxito")
        );
    }

    /**
     * Teacher checks in (basic - without schedule details)
     */
    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<TeacherAttendanceEntity>> checkIn(
            @RequestParam UUID teacherUuid,
            @RequestParam UUID classSessionUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate attendanceDate = date != null ? date : LocalDate.now();
        TeacherAttendanceEntity attendance = attendanceService.checkIn(teacherUuid, classSessionUuid, attendanceDate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(attendance, "Entrada registrada con éxito"));
    }

    /**
     * Teacher checks in with full schedule details (calculates penalties)
     */
    @PostMapping("/check-in-with-schedule")
    public ResponseEntity<ApiResponse<TeacherAttendanceEntity>> checkInWithSchedule(
            @RequestParam UUID teacherUuid,
            @RequestParam UUID classSessionUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime scheduledStartTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime scheduledEndTime,
            @RequestParam Integer scheduledDurationMinutes) {
        LocalDate attendanceDate = date != null ? date : LocalDate.now();
        TeacherAttendanceEntity attendance = attendanceService.checkInWithSchedule(
                teacherUuid, classSessionUuid, attendanceDate, scheduledStartTime, scheduledEndTime, scheduledDurationMinutes);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(attendance, "Entrada registrada con éxito"));
    }

    /**
     * Teacher checks out
     */
    @PatchMapping("/{uuid}/check-out")
    public ResponseEntity<ApiResponse<TeacherAttendanceEntity>> checkOut(@PathVariable UUID uuid) {
        TeacherAttendanceEntity attendance = attendanceService.checkOut(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(attendance, "Salida registrada con éxito")
        );
    }

    /**
     * Admin approves attendance
     */
    @PatchMapping("/{uuid}/approve")
    public ResponseEntity<ApiResponse<TeacherAttendanceEntity>> approveAttendance(
            @PathVariable UUID uuid,
            @RequestParam(required = false) String adminNote) {
        TeacherAttendanceEntity attendance = attendanceService.approveAttendance(uuid, adminNote);
        return ResponseEntity.ok(
                ApiResponse.success(attendance, "Asistencia aprobada con éxito")
        );
    }

    /**
     * Admin overrides attendance (manual correction)
     * TODO: Replace parameters with AttendanceOverrideRequestDTO when DTOs are created
     */
    @PatchMapping("/{uuid}/override")
    public ResponseEntity<ApiResponse<TeacherAttendanceEntity>> overrideAttendance(
            @PathVariable UUID uuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkinAt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkoutAt,
            @RequestParam(defaultValue = "false") boolean resetPenalties,
            @RequestParam(required = false) String adminNote) {
        TeacherAttendanceEntity attendance = attendanceService.overrideAttendance(
                uuid, checkinAt, checkoutAt, resetPenalties, adminNote);
        return ResponseEntity.ok(
                ApiResponse.success(attendance, "Asistencia modificada por administrador con éxito")
        );
    }

    /**
     * Mark attendance as holiday (auto-fills attendance with full scheduled time)
     */
    @PatchMapping("/{uuid}/mark-holiday")
    public ResponseEntity<ApiResponse<TeacherAttendanceEntity>> markAsHoliday(
            @PathVariable UUID uuid,
            @RequestParam(required = false) String adminNote) {
        TeacherAttendanceEntity attendance = attendanceService.markAsHoliday(uuid, adminNote);
        return ResponseEntity.ok(
                ApiResponse.success(attendance, "Asistencia marcada como feriado con éxito")
        );
    }

    /**
     * Reject attendance
     */
    @PatchMapping("/{uuid}/reject")
    public ResponseEntity<ApiResponse<TeacherAttendanceEntity>> rejectAttendance(
            @PathVariable UUID uuid,
            @RequestParam(required = false) String adminNote) {
        TeacherAttendanceEntity attendance = attendanceService.rejectAttendance(uuid, adminNote);
        return ResponseEntity.ok(
                ApiResponse.success(attendance, "Asistencia rechazada con éxito")
        );
    }

    /**
     * Calculate total minutes worked by a teacher in a date range
     */
    @GetMapping("/teacher/{teacherUuid}/total-minutes-worked")
    public ResponseEntity<ApiResponse<Integer>> calculateTotalMinutesWorked(
            @PathVariable UUID teacherUuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        int totalMinutes = attendanceService.calculateTotalMinutesWorked(teacherUuid, startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success(totalMinutes, "Total de minutos trabajados calculado con éxito")
        );
    }

    /**
     * Calculate total penalty minutes in a date range
     */
    @GetMapping("/teacher/{teacherUuid}/total-penalty-minutes")
    public ResponseEntity<ApiResponse<Integer>> calculateTotalPenaltyMinutes(
            @PathVariable UUID teacherUuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        int totalPenaltyMinutes = attendanceService.calculateTotalPenaltyMinutes(teacherUuid, startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success(totalPenaltyMinutes, "Total de minutos de penalización calculado con éxito")
        );
    }

    /**
     * Get attendance statistics for a teacher in a date range
     */
    @GetMapping("/teacher/{teacherUuid}/statistics")
    public ResponseEntity<ApiResponse<TeacherAttendanceService.AttendanceStats>> getAttendanceStats(
            @PathVariable UUID teacherUuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        TeacherAttendanceService.AttendanceStats stats = attendanceService.getAttendanceStats(
                teacherUuid, startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success(stats, "Estadísticas de asistencia calculadas con éxito")
        );
    }
}
