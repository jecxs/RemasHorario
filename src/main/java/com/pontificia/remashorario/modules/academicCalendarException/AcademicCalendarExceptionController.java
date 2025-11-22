package com.pontificia.remashorario.modules.academicCalendarException;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.academicCalendarException.dto.AcademicCalendarExceptionRequestDTO;
import com.pontificia.remashorario.modules.academicCalendarException.dto.AcademicCalendarExceptionResponseDTO;
import com.pontificia.remashorario.modules.academicCalendarException.dto.BulkCalendarExceptionRequestDTO;
import com.pontificia.remashorario.modules.academicCalendarException.mapper.AcademicCalendarExceptionMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for managing academic calendar exceptions (holidays, special dates)
 * Used to mark dates that should not count as regular working days
 */
@RestController
@RequestMapping("/api/protected/calendar-exceptions")
@RequiredArgsConstructor
public class AcademicCalendarExceptionController {

    private final AcademicCalendarExceptionService exceptionService;
    private final AcademicCalendarExceptionMapper exceptionMapper;

    /**
     * Get all calendar exceptions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AcademicCalendarExceptionResponseDTO>>> getAllExceptions() {
        List<AcademicCalendarExceptionEntity> exceptions = exceptionService.getAllExceptions();
        List<AcademicCalendarExceptionResponseDTO> responseDTOs = exceptionMapper.toResponseDTOList(exceptions);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Excepciones de calendario recuperadas con éxito")
        );
    }

    /**
     * Get exception by ID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<AcademicCalendarExceptionResponseDTO>> getExceptionById(@PathVariable UUID uuid) {
        AcademicCalendarExceptionEntity exception = exceptionService.getExceptionById(uuid);
        AcademicCalendarExceptionResponseDTO responseDTO = exceptionMapper.toResponseDTO(exception);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Excepción de calendario recuperada con éxito")
        );
    }

    /**
     * Get exception by specific date
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<AcademicCalendarExceptionResponseDTO>> getExceptionByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AcademicCalendarExceptionEntity exception = exceptionService.getExceptionByDate(date);
        AcademicCalendarExceptionResponseDTO responseDTO = exceptionMapper.toResponseDTO(exception);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Excepción de calendario recuperada con éxito")
        );
    }

    /**
     * Get exceptions in a date range
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<AcademicCalendarExceptionResponseDTO>>> getExceptionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AcademicCalendarExceptionEntity> exceptions = exceptionService.getExceptionsByDateRange(startDate, endDate);
        List<AcademicCalendarExceptionResponseDTO> responseDTOs = exceptionMapper.toResponseDTOList(exceptions);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Excepciones de calendario recuperadas con éxito")
        );
    }

    /**
     * Get upcoming exceptions from a specific date
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<AcademicCalendarExceptionResponseDTO>>> getUpcomingExceptions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate) {
        LocalDate from = fromDate != null ? fromDate : LocalDate.now();
        List<AcademicCalendarExceptionEntity> exceptions = exceptionService.getUpcomingExceptions(from);
        List<AcademicCalendarExceptionResponseDTO> responseDTOs = exceptionMapper.toResponseDTOList(exceptions);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Excepciones próximas recuperadas con éxito")
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
    public ResponseEntity<ApiResponse<List<AcademicCalendarExceptionResponseDTO>>> getHolidaysInMonth(
            @PathVariable int year,
            @PathVariable int month) {
        List<AcademicCalendarExceptionEntity> holidays = exceptionService.getHolidaysInMonth(year, month);
        List<AcademicCalendarExceptionResponseDTO> responseDTOs = exceptionMapper.toResponseDTOList(holidays);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTOs, "Feriados del mes recuperados con éxito")
        );
    }

    /**
     * Create new calendar exception
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AcademicCalendarExceptionResponseDTO>> createException(
            @Valid @RequestBody AcademicCalendarExceptionRequestDTO requestDTO) {
        AcademicCalendarExceptionEntity exception = exceptionService.createException(
                requestDTO.getDate(),
                requestDTO.getCode(),
                requestDTO.getDescription()
        );
        AcademicCalendarExceptionResponseDTO responseDTO = exceptionMapper.toResponseDTO(exception);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTO, "Excepción de calendario creada con éxito"));
    }

    /**
     * Update calendar exception
     */
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<AcademicCalendarExceptionResponseDTO>> updateException(
            @PathVariable UUID uuid,
            @Valid @RequestBody AcademicCalendarExceptionRequestDTO requestDTO) {
        AcademicCalendarExceptionEntity exception = exceptionService.updateException(
                uuid,
                requestDTO.getDate(),
                requestDTO.getCode(),
                requestDTO.getDescription()
        );
        AcademicCalendarExceptionResponseDTO responseDTO = exceptionMapper.toResponseDTO(exception);
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Excepción de calendario actualizada con éxito")
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
     */
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<AcademicCalendarExceptionResponseDTO>>> createBulkExceptions(
            @Valid @RequestBody BulkCalendarExceptionRequestDTO bulkRequestDTO) {
        List<AcademicCalendarExceptionEntity> entitiesToCreate = bulkRequestDTO.getExceptions().stream()
                .map(exceptionMapper::toEntity)
                .collect(Collectors.toList());

        List<AcademicCalendarExceptionEntity> created = exceptionService.createBulkExceptions(entitiesToCreate);
        List<AcademicCalendarExceptionResponseDTO> responseDTOs = exceptionMapper.toResponseDTOList(created);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTOs, "Excepciones de calendario creadas en masa con éxito"));
    }
}
