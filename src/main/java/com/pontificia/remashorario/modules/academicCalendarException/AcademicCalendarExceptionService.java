package com.pontificia.remashorario.modules.academicCalendarException;

import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing academic calendar exceptions (holidays, special dates)
 * Used to mark dates as holidays to adjust teacher attendance and payroll
 */
@Service
public class AcademicCalendarExceptionService extends BaseService<AcademicCalendarExceptionEntity> {

    private final AcademicCalendarExceptionRepository exceptionRepository;

    @Autowired
    public AcademicCalendarExceptionService(AcademicCalendarExceptionRepository exceptionRepository) {
        super(exceptionRepository);
        this.exceptionRepository = exceptionRepository;
    }

    public List<AcademicCalendarExceptionEntity> getAllExceptions() {
        return findAll();
    }

    public AcademicCalendarExceptionEntity getExceptionById(UUID uuid) {
        return findExceptionOrThrow(uuid);
    }

    public AcademicCalendarExceptionEntity getExceptionByDate(LocalDate date) {
        return exceptionRepository.findByDate(date)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró excepción para la fecha: " + date));
    }

    public List<AcademicCalendarExceptionEntity> getExceptionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return exceptionRepository.findByDateRange(startDate, endDate);
    }

    public List<AcademicCalendarExceptionEntity> getUpcomingExceptions(LocalDate fromDate) {
        return exceptionRepository.findByDateAfter(fromDate);
    }

    public AcademicCalendarExceptionEntity findExceptionOrThrow(UUID uuid) {
        return findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Excepción de calendario no encontrada con ID: " + uuid));
    }

    /**
     * Check if a specific date is a holiday
     */
    public boolean isHoliday(LocalDate date) {
        return exceptionRepository.existsByDate(date);
    }

    @Transactional
    public AcademicCalendarExceptionEntity createException(LocalDate date, String code, String description) {
        // Validate unique date
        if (exceptionRepository.existsByDate(date)) {
            throw new IllegalArgumentException("Ya existe una excepción para la fecha: " + date);
        }

        AcademicCalendarExceptionEntity exception = new AcademicCalendarExceptionEntity();
        exception.setDate(date);
        exception.setCode(code);
        exception.setDescription(description);

        return save(exception);
    }

    @Transactional
    public AcademicCalendarExceptionEntity updateException(UUID uuid, LocalDate date, String code, String description) {
        AcademicCalendarExceptionEntity exception = findExceptionOrThrow(uuid);

        // Validate unique date if it changes
        if (!exception.getDate().equals(date) && exceptionRepository.existsByDate(date)) {
            throw new IllegalArgumentException("Ya existe una excepción para la fecha: " + date);
        }

        exception.setDate(date);
        exception.setCode(code);
        exception.setDescription(description);

        return save(exception);
    }

    @Transactional
    public void deleteException(UUID uuid) {
        AcademicCalendarExceptionEntity exception = findExceptionOrThrow(uuid);
        // TODO: Validate no attendances are marked with this exception
        deleteById(uuid);
    }

    /**
     * Create multiple exceptions at once (useful for importing holiday calendar)
     */
    @Transactional
    public List<AcademicCalendarExceptionEntity> createBulkExceptions(List<AcademicCalendarExceptionEntity> exceptions) {
        for (AcademicCalendarExceptionEntity exception : exceptions) {
            if (exceptionRepository.existsByDate(exception.getDate())) {
                throw new IllegalArgumentException("Ya existe una excepción para la fecha: " + exception.getDate());
            }
        }
        return saveAll(exceptions);
    }

    /**
     * Get all holidays in a specific month
     */
    public List<AcademicCalendarExceptionEntity> getHolidaysInMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return exceptionRepository.findByDateRange(startDate, endDate);
    }
}
