package com.pontificia.remashorario.modules.teacherAttendance;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.teacherAttendance.dto.*;
import com.pontificia.remashorario.modules.teacherAttendance.mapper.TeacherAttendanceMapper;
import jakarta.validation.Valid;
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
    private final TeacherAttendanceMapper attendanceMapper;

    /**
     * Get all attendances
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TeacherAttendanceResponseDTO>>> getAllAttendances() {
        List<TeacherAttendanceEntity> attendances = attendanceService.getAllAttendances();
        List<TeacherAttendanceResponseDTO> responseDTOs = attendanceMapper.toResponseDTOList(attendances);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Asistencias recuperadas con éxito")
        );
    }

    /**
     * Get attendance by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<TeacherAttendanceResponseDTO>> getAttendanceById(@PathVariable UUID uuid) {
        TeacherAttendanceEntity attendance = attendanceService.getAttendanceById(uuid);
        TeacherAttendanceResponseDTO responseDTO = attendanceMapper.toResponseDTO(attendance);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Asistencia recuperada con éxito")
        );
    }

    /**
     * Get attendance by ID with full details
     */
    @GetMapping("/{uuid}/details")
    public ResponseEntity<ApiResponse<TeacherAttendanceResponseDTO>> getAttendanceByIdWithDetails(@PathVariable UUID uuid) {
        TeacherAttendanceEntity attendance = attendanceService.getAttendanceByIdWithDetails(uuid);
        TeacherAttendanceResponseDTO responseDTO = attendanceMapper.toResponseDTO(attendance);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Asistencia con detalles recuperada con éxito")
        );
    }

    /**
     * Get all attendances for a teacher
     */
    @GetMapping("/teacher/{teacherUuid}")
    public ResponseEntity<ApiResponse<List<TeacherAttendanceResponseDTO>>> getAttendancesByTeacher(
            @PathVariable UUID teacherUuid) {
        List<TeacherAttendanceEntity> attendances = attendanceService.getAttendancesByTeacher(teacherUuid);
        List<TeacherAttendanceResponseDTO> responseDTOs = attendanceMapper.toResponseDTOList(attendances);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Asistencias del docente recuperadas con éxito")
        );
    }

    /**
     * Get attendances for a teacher on a specific date
     */
    @GetMapping("/teacher/{teacherUuid}/date/{date}")
    public ResponseEntity<ApiResponse<List<TeacherAttendanceResponseDTO>>> getAttendancesByTeacherAndDate(
            @PathVariable UUID teacherUuid,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TeacherAttendanceEntity> attendances = attendanceService.getAttendancesByTeacherAndDate(teacherUuid, date);
        List<TeacherAttendanceResponseDTO> responseDTOs = attendanceMapper.toResponseDTOList(attendances);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Asistencias del docente en la fecha recuperadas con éxito")
        );
    }

    /**
     * Get attendances for a teacher in a date range
     */
    @GetMapping("/teacher/{teacherUuid}/range")
    public ResponseEntity<ApiResponse<List<TeacherAttendanceResponseDTO>>> getAttendancesByTeacherAndDateRange(
            @PathVariable UUID teacherUuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TeacherAttendanceEntity> attendances = attendanceService.getAttendancesByTeacherAndDateRange(
                teacherUuid, startDate, endDate);
        List<TeacherAttendanceResponseDTO> responseDTOs = attendanceMapper.toResponseDTOList(attendances);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Asistencias del docente en el rango recuperadas con éxito")
        );
    }

    /**
     * Get attendances in a date range
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<TeacherAttendanceResponseDTO>>> getAttendancesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TeacherAttendanceEntity> attendances = attendanceService.getAttendancesByDateRange(startDate, endDate);
        List<TeacherAttendanceResponseDTO> responseDTOs = attendanceMapper.toResponseDTOList(attendances);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Asistencias en el rango recuperadas con éxito")
        );
    }

    /**
     * Get pending attendances for a teacher
     */
    @GetMapping("/teacher/{teacherUuid}/pending")
    public ResponseEntity<ApiResponse<List<TeacherAttendanceResponseDTO>>> getPendingAttendancesByTeacher(
            @PathVariable UUID teacherUuid) {
        List<TeacherAttendanceEntity> attendances = attendanceService.getPendingAttendancesByTeacher(teacherUuid);
        List<TeacherAttendanceResponseDTO> responseDTOs = attendanceMapper.toResponseDTOList(attendances);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Asistencias pendientes del docente recuperadas con éxito")
        );
    }

    /**
     * Teacher checks in (basic - without schedule details)
     */
    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<TeacherAttendanceResponseDTO>> checkIn(
            @Valid @RequestBody TeacherAttendanceCheckInRequestDTO requestDTO) {
        LocalDate attendanceDate = requestDTO.getAttendanceDate() != null ? requestDTO.getAttendanceDate() : LocalDate.now();
        TeacherAttendanceEntity attendance = attendanceService.checkIn(
                requestDTO.getTeacherUuid(),
                requestDTO.getClassSessionUuid(),
                attendanceDate
        );
        TeacherAttendanceResponseDTO responseDTO = attendanceMapper.toResponseDTO(attendance);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTO, "Entrada registrada con éxito"));
    }

    /**
     * Teacher checks in with full schedule details (calculates penalties)
     */
    @PostMapping("/check-in-with-schedule")
    public ResponseEntity<ApiResponse<TeacherAttendanceResponseDTO>> checkInWithSchedule(
            @Valid @RequestBody TeacherAttendanceCheckInWithScheduleRequestDTO requestDTO) {
        LocalDate attendanceDate = requestDTO.getAttendanceDate() != null ? requestDTO.getAttendanceDate() : LocalDate.now();
        TeacherAttendanceEntity attendance = attendanceService.checkInWithSchedule(
                requestDTO.getTeacherUuid(),
                requestDTO.getClassSessionUuid(),
                attendanceDate,
                requestDTO.getScheduledStartTime(),
                requestDTO.getScheduledEndTime(),
                requestDTO.getScheduledDurationMinutes()
        );
        TeacherAttendanceResponseDTO responseDTO = attendanceMapper.toResponseDTO(attendance);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTO, "Entrada registrada con éxito"));
    }

    /**
     * Teacher checks out
     */
    @PatchMapping("/{uuid}/check-out")
    public ResponseEntity<ApiResponse<TeacherAttendanceResponseDTO>> checkOut(@PathVariable UUID uuid) {
        TeacherAttendanceEntity attendance = attendanceService.checkOut(uuid);
        TeacherAttendanceResponseDTO responseDTO = attendanceMapper.toResponseDTO(attendance);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Salida registrada con éxito")
        );
    }

    /**
     * Admin approves attendance
     */
    @PatchMapping("/{uuid}/approve")
    public ResponseEntity<ApiResponse<TeacherAttendanceResponseDTO>> approveAttendance(
            @PathVariable UUID uuid,
            @RequestParam(required = false) String adminNote) {
        TeacherAttendanceEntity attendance = attendanceService.approveAttendance(uuid, adminNote);
        TeacherAttendanceResponseDTO responseDTO = attendanceMapper.toResponseDTO(attendance);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Asistencia aprobada con éxito")
        );
    }

    /**
     * Admin overrides attendance (manual correction)
     */
    @PatchMapping("/{uuid}/override")
    public ResponseEntity<ApiResponse<TeacherAttendanceResponseDTO>> overrideAttendance(
            @PathVariable UUID uuid,
            @Valid @RequestBody AttendanceOverrideRequestDTO requestDTO) {
        TeacherAttendanceEntity attendance = attendanceService.overrideAttendance(
                uuid,
                requestDTO.getCheckinAt(),
                requestDTO.getCheckoutAt(),
                requestDTO.getResetPenalties(),
                requestDTO.getAdminNote()
        );
        TeacherAttendanceResponseDTO responseDTO = attendanceMapper.toResponseDTO(attendance);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Asistencia modificada por administrador con éxito")
        );
    }

    /**
     * Mark attendance as holiday (auto-fills attendance with full scheduled time)
     */
    @PatchMapping("/{uuid}/mark-holiday")
    public ResponseEntity<ApiResponse<TeacherAttendanceResponseDTO>> markAsHoliday(
            @PathVariable UUID uuid,
            @RequestParam(required = false) String adminNote) {
        TeacherAttendanceEntity attendance = attendanceService.markAsHoliday(uuid, adminNote);
        TeacherAttendanceResponseDTO responseDTO = attendanceMapper.toResponseDTO(attendance);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Asistencia marcada como feriado con éxito")
        );
    }

    /**
     * Reject attendance
     */
    @PatchMapping("/{uuid}/reject")
    public ResponseEntity<ApiResponse<TeacherAttendanceResponseDTO>> rejectAttendance(
            @PathVariable UUID uuid,
            @RequestParam(required = false) String adminNote) {
        TeacherAttendanceEntity attendance = attendanceService.rejectAttendance(uuid, adminNote);
        TeacherAttendanceResponseDTO responseDTO = attendanceMapper.toResponseDTO(attendance);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Asistencia rechazada con éxito")
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
    public ResponseEntity<ApiResponse<AttendanceStatisticsDTO>> getAttendanceStats(
            @PathVariable UUID teacherUuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        TeacherAttendanceService.AttendanceStats stats = attendanceService.getAttendanceStats(
                teacherUuid, startDate, endDate);
        AttendanceStatisticsDTO statsDTO = attendanceMapper.toStatisticsDTO(stats);
        return ResponseEntity.ok(
                ApiResponse.success(statsDTO, "Estadísticas de asistencia calculadas con éxito")
        );
    }
}
