package com.pontificia.remashorario.modules.academicCalendarException;

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
 * REST Controller for managing academic calendar exceptions (holidays, special dates)
 * Used to mark dates that should not count as regular working days
 */
@RestController
@RequestMapping("/api/protected/calendar-exceptions")
@RequiredArgsConstructor
public class AcademicCalendarExceptionController {

    private final AcademicCalendarExceptionService exceptionService;

    /**
     * Get all calendar exceptions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AcademicCalendarExceptionEntity>>> getAllExceptions() {
        List<AcademicCalendarExceptionEntity> exceptions = exceptionService.getAllExceptions();
        return ResponseEntity.ok(
                ApiResponse.success(exceptions, "Excepciones de calendario recuperadas con éxito")
        );
    }

    /**
     * Get exception by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<AcademicCalendarExceptionEntity>> getExceptionById(@PathVariable UUID uuid) {
        AcademicCalendarExceptionEntity exception = exceptionService.getExceptionById(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(exception, "Excepción de calendario recuperada con éxito")
        );
    }

    /**
     * Get exception by specific date
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<AcademicCalendarExceptionEntity>> getExceptionByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AcademicCalendarExceptionEntity exception = exceptionService.getExceptionByDate(date);
        return ResponseEntity.ok(
                ApiResponse.success(exception, "Excepción de calendario recuperada con éxito")
        );
    }

    /**
     * Get exceptions in a date range
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<AcademicCalendarExceptionEntity>>> getExceptionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AcademicCalendarExceptionEntity> exceptions = exceptionService.getExceptionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success(exceptions, "Excepciones de calendario recuperadas con éxito")
        );
    }

    /**
     * Get upcoming exceptions from a specific date
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<AcademicCalendarExceptionEntity>>> getUpcomingExceptions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate) {
        LocalDate from = fromDate != null ? fromDate : LocalDate.now();
        List<AcademicCalendarExceptionEntity> exceptions = exceptionService.getUpcomingExceptions(from);
        return ResponseEntity.ok(
                ApiResponse.success(exceptions, "Excepciones próximas recuperadas con éxito")
        );
    }

    /**
     * Check if a specific date is a holiday
     */
    @GetMapping("/is-holiday/{date}")
    public ResponseEntity<ApiResponse<Boolean>> isHoliday(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        boolean isHoliday = exceptionService.isHoliday(date);
        return ResponseEntity.ok(
                ApiResponse.success(isHoliday, "Verificación de feriado realizada con éxito")
        );
    }

    /**
     * Get holidays in a specific month
     */
    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<ApiResponse<List<AcademicCalendarExceptionEntity>>> getHolidaysInMonth(
            @PathVariable int year,
            @PathVariable int month) {
        List<AcademicCalendarExceptionEntity> holidays = exceptionService.getHolidaysInMonth(year, month);
        return ResponseEntity.ok(
                ApiResponse.success(holidays, "Feriados del mes recuperados con éxito")
        );
    }

    /**
     * Create new calendar exception
     * TODO: Replace parameters with CalendarExceptionRequestDTO when DTOs are created
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AcademicCalendarExceptionEntity>> createException(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String code,
            @RequestParam(required = false) String description) {
        AcademicCalendarExceptionEntity exception = exceptionService.createException(date, code, description);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(exception, "Excepción de calendario creada con éxito"));
    }

    /**
     * Update calendar exception
     * TODO: Replace parameters with CalendarExceptionRequestDTO when DTOs are created
     */
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<AcademicCalendarExceptionEntity>> updateException(
            @PathVariable UUID uuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String code,
            @RequestParam(required = false) String description) {
        AcademicCalendarExceptionEntity exception = exceptionService.updateException(uuid, date, code, description);
        return ResponseEntity.ok(
                ApiResponse.success(exception, "Excepción de calendario actualizada con éxito")
        );
    }

    /**
     * Delete calendar exception
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteException(@PathVariable UUID uuid) {
        exceptionService.deleteException(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Excepción de calendario eliminada con éxito")
        );
    }

    /**
     * Create multiple exceptions at once (bulk import)
     * TODO: Replace List<AcademicCalendarExceptionEntity> with BulkCalendarExceptionRequestDTO when DTOs are created
     */
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<AcademicCalendarExceptionEntity>>> createBulkExceptions(
            @RequestBody List<AcademicCalendarExceptionEntity> exceptions) {
        List<AcademicCalendarExceptionEntity> created = exceptionService.createBulkExceptions(exceptions);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Excepciones de calendario creadas en masa con éxito"));
    }
}
