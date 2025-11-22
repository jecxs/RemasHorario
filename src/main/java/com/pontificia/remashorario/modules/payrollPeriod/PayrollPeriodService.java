package com.pontificia.remashorario.modules.payrollPeriod;

import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing payroll periods (weekly, biweekly, monthly)
 * Controls the lifecycle of payroll calculation periods
 */
@Service
public class PayrollPeriodService extends BaseService<PayrollPeriodEntity> {

    private final PayrollPeriodRepository payrollPeriodRepository;

    @Autowired
    public PayrollPeriodService(PayrollPeriodRepository payrollPeriodRepository) {
        super(payrollPeriodRepository);
        this.payrollPeriodRepository = payrollPeriodRepository;
    }

    public List<PayrollPeriodEntity> getAllPeriods() {
        return payrollPeriodRepository.findAllOrderByStartDateDesc();
    }

    public PayrollPeriodEntity getPeriodById(UUID uuid) {
        return findPeriodOrThrow(uuid);
    }

    public List<PayrollPeriodEntity> getPeriodsByStatus(PayrollPeriodEntity.PayrollStatus status) {
        return payrollPeriodRepository.findByStatus(status);
    }

    public PayrollPeriodEntity getPeriodByDate(LocalDate date) {
        return payrollPeriodRepository.findByDate(date)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró período de nómina para la fecha: " + date));
    }

    public List<PayrollPeriodEntity> getPendingPeriods() {
        return payrollPeriodRepository.findPendingPeriods();
    }

    public List<PayrollPeriodEntity> getPastPeriods(LocalDate date) {
        return payrollPeriodRepository.findPastPeriods(date);
    }

    public List<PayrollPeriodEntity> getFuturePeriods(LocalDate date) {
        return payrollPeriodRepository.findFuturePeriods(date);
    }

    public PayrollPeriodEntity findPeriodOrThrow(UUID uuid) {
        return findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Período de nómina no encontrado con ID: " + uuid));
    }

    @Transactional
    public PayrollPeriodEntity createPeriod(String name, LocalDate startDate, LocalDate endDate) {
        // Validate date range
        if (startDate.isAfter(endDate) || startDate.equals(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }

        // Validate no overlapping periods
        if (payrollPeriodRepository.existsOverlappingPeriod(startDate, endDate)) {
            throw new IllegalArgumentException("Ya existe un período que se superpone con las fechas especificadas");
        }

        PayrollPeriodEntity period = new PayrollPeriodEntity();
        period.setName(name);
        period.setStartDate(startDate);
        period.setEndDate(endDate);
        period.setStatus(PayrollPeriodEntity.PayrollStatus.DRAFT);

        return save(period);
    }

    @Transactional
    public PayrollPeriodEntity updatePeriod(UUID uuid, String name, LocalDate startDate, LocalDate endDate) {
        PayrollPeriodEntity period = findPeriodOrThrow(uuid);

        // Only allow updates if status is DRAFT
        if (period.getStatus() != PayrollPeriodEntity.PayrollStatus.DRAFT) {
            throw new IllegalStateException("Solo se pueden modificar períodos en estado BORRADOR");
        }

        // Validate date range
        if (startDate.isAfter(endDate) || startDate.equals(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }

        period.setName(name);
        period.setStartDate(startDate);
        period.setEndDate(endDate);

        return save(period);
    }

    @Transactional
    public void deletePeriod(UUID uuid) {
        PayrollPeriodEntity period = findPeriodOrThrow(uuid);

        // Only allow deletion if status is DRAFT
        if (period.getStatus() != PayrollPeriodEntity.PayrollStatus.DRAFT) {
            throw new IllegalStateException("Solo se pueden eliminar períodos en estado BORRADOR");
        }

        deleteById(uuid);
    }

    /**
     * Transition period to CALCULATED status
     */
    @Transactional
    public PayrollPeriodEntity markAsCalculated(UUID uuid) {
        PayrollPeriodEntity period = findPeriodOrThrow(uuid);

        if (period.getStatus() != PayrollPeriodEntity.PayrollStatus.DRAFT) {
            throw new IllegalStateException("Solo se puede calcular un período en estado BORRADOR");
        }

        period.setStatus(PayrollPeriodEntity.PayrollStatus.CALCULATED);
        return save(period);
    }

    /**
     * Transition period to APPROVED status
     */
    @Transactional
    public PayrollPeriodEntity markAsApproved(UUID uuid) {
        PayrollPeriodEntity period = findPeriodOrThrow(uuid);

        if (period.getStatus() != PayrollPeriodEntity.PayrollStatus.CALCULATED) {
            throw new IllegalStateException("Solo se puede aprobar un período en estado CALCULADO");
        }

        period.setStatus(PayrollPeriodEntity.PayrollStatus.APPROVED);
        return save(period);
    }

    /**
     * Transition period to PAID status
     */
    @Transactional
    public PayrollPeriodEntity markAsPaid(UUID uuid) {
        PayrollPeriodEntity period = findPeriodOrThrow(uuid);

        if (period.getStatus() != PayrollPeriodEntity.PayrollStatus.APPROVED) {
            throw new IllegalStateException("Solo se puede marcar como pagado un período en estado APROBADO");
        }

        period.setStatus(PayrollPeriodEntity.PayrollStatus.PAID);
        return save(period);
    }

    /**
     * Revert period back to DRAFT status (for recalculation)
     */
    @Transactional
    public PayrollPeriodEntity revertToDraft(UUID uuid) {
        PayrollPeriodEntity period = findPeriodOrThrow(uuid);

        if (period.getStatus() == PayrollPeriodEntity.PayrollStatus.PAID) {
            throw new IllegalStateException("No se puede revertir un período que ya fue pagado");
        }

        period.setStatus(PayrollPeriodEntity.PayrollStatus.DRAFT);
        return save(period);
    }

    /**
     * Create weekly periods for a month
     */
    @Transactional
    public List<PayrollPeriodEntity> createWeeklyPeriodsForMonth(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        List<PayrollPeriodEntity> periods = new java.util.ArrayList<>();
        LocalDate currentStart = startOfMonth;
        int weekNumber = 1;

        while (currentStart.isBefore(endOfMonth) || currentStart.equals(endOfMonth)) {
            LocalDate currentEnd = currentStart.plusWeeks(1).minusDays(1);
            if (currentEnd.isAfter(endOfMonth)) {
                currentEnd = endOfMonth;
            }

            String name = String.format("Semana %d - %s %d", weekNumber, startOfMonth.getMonth(), year);
            PayrollPeriodEntity period = createPeriod(name, currentStart, currentEnd);
            periods.add(period);

            currentStart = currentEnd.plusDays(1);
            weekNumber++;
        }

        return periods;
    }

    /**
     * Create biweekly periods for a month
     */
    @Transactional
    public List<PayrollPeriodEntity> createBiweeklyPeriodsForMonth(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        List<PayrollPeriodEntity> periods = new java.util.ArrayList<>();

        // First period: 1st to 15th
        LocalDate firstPeriodEnd = LocalDate.of(year, month, 15);
        if (firstPeriodEnd.isAfter(endOfMonth)) {
            firstPeriodEnd = endOfMonth;
        }
        String firstName = String.format("Quincena 1 - %s %d", startOfMonth.getMonth(), year);
        periods.add(createPeriod(firstName, startOfMonth, firstPeriodEnd));

        // Second period: 16th to end of month (if exists)
        if (firstPeriodEnd.isBefore(endOfMonth)) {
            LocalDate secondPeriodStart = firstPeriodEnd.plusDays(1);
            String secondName = String.format("Quincena 2 - %s %d", startOfMonth.getMonth(), year);
            periods.add(createPeriod(secondName, secondPeriodStart, endOfMonth));
        }

        return periods;
    }

    /**
     * Create monthly period
     */
    @Transactional
    public PayrollPeriodEntity createMonthlyPeriod(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        String name = String.format("%s %d", startOfMonth.getMonth(), year);
        return createPeriod(name, startOfMonth, endOfMonth);
    }

    /**
     * Check if a period can be modified
     */
    public boolean canModify(UUID uuid) {
        PayrollPeriodEntity period = findPeriodOrThrow(uuid);
        return period.getStatus() == PayrollPeriodEntity.PayrollStatus.DRAFT;
    }

    /**
     * Check if a period can be deleted
     */
    public boolean canDelete(UUID uuid) {
        return canModify(uuid);
    }
}
