package com.pontificia.remashorario.modules.defaultRate;

import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeEntity;
import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeService;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing default hourly rates
 * These are fallback rates used when no specific teacher or modality rate is defined
 */
@Service
public class DefaultRateService extends BaseService<DefaultRateEntity> {

    private final DefaultRateRepository defaultRateRepository;
    private final AttendanceActivityTypeService activityTypeService;

    @Autowired
    public DefaultRateService(DefaultRateRepository defaultRateRepository,
                             AttendanceActivityTypeService activityTypeService) {
        super(defaultRateRepository);
        this.defaultRateRepository = defaultRateRepository;
        this.activityTypeService = activityTypeService;
    }

    public List<DefaultRateEntity> getAllRates() {
        return findAll();
    }

    public DefaultRateEntity getRateById(UUID uuid) {
        return findRateOrThrow(uuid);
    }

    public DefaultRateEntity getRateByIdWithDetails(UUID uuid) {
        return defaultRateRepository.findByIdWithDetails(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Tarifa por defecto no encontrada con ID: " + uuid));
    }

    public List<DefaultRateEntity> getRatesByActivityType(UUID activityTypeUuid) {
        return defaultRateRepository.findByActivityTypeUuid(activityTypeUuid);
    }

    /**
     * Get the active default rate for a specific activity type on a given date
     */
    public DefaultRateEntity getActiveRateByActivityType(UUID activityTypeUuid, LocalDate date) {
        return defaultRateRepository.findActiveRateByActivityType(activityTypeUuid, date)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró tarifa por defecto activa para el tipo de actividad en la fecha: " + date));
    }

    /**
     * Get all active rates for a given date
     */
    public List<DefaultRateEntity> getActiveRates(LocalDate date) {
        return defaultRateRepository.findActiveRates(date);
    }

    public DefaultRateEntity findRateOrThrow(UUID uuid) {
        return findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Tarifa por defecto no encontrada con ID: " + uuid));
    }

    @Transactional
    public DefaultRateEntity createRate(UUID activityTypeUuid, BigDecimal ratePerHour,
                                       LocalDate effectiveFrom, LocalDate effectiveTo) {
        // Validate activity type exists
        AttendanceActivityTypeEntity activityType = activityTypeService.findActivityTypeOrThrow(activityTypeUuid);

        // Validate rate amount
        if (ratePerHour.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La tarifa por hora debe ser mayor a cero");
        }

        // Validate date range
        if (effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        DefaultRateEntity rate = new DefaultRateEntity();
        rate.setActivityType(activityType);
        rate.setRatePerHour(ratePerHour);
        rate.setEffectiveFrom(effectiveFrom);
        rate.setEffectiveTo(effectiveTo);

        return save(rate);
    }

    @Transactional
    public DefaultRateEntity updateRate(UUID uuid, BigDecimal ratePerHour,
                                       LocalDate effectiveFrom, LocalDate effectiveTo) {
        DefaultRateEntity rate = findRateOrThrow(uuid);

        // Validate rate amount
        if (ratePerHour.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La tarifa por hora debe ser mayor a cero");
        }

        // Validate date range
        if (effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        rate.setRatePerHour(ratePerHour);
        rate.setEffectiveFrom(effectiveFrom);
        rate.setEffectiveTo(effectiveTo);

        return save(rate);
    }

    @Transactional
    public void deleteRate(UUID uuid) {
        DefaultRateEntity rate = findRateOrThrow(uuid);
        // TODO: Validate no payrolls are using this rate
        deleteById(uuid);
    }

    /**
     * Get the applicable rate per minute for calculations
     * Converts hourly rate to per-minute rate
     */
    public BigDecimal getRatePerMinute(UUID activityTypeUuid, LocalDate date) {
        DefaultRateEntity rate = getActiveRateByActivityType(activityTypeUuid, date);
        return rate.getRatePerHour().divide(BigDecimal.valueOf(60), 4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Close a rate by setting its effectiveTo date to today
     */
    @Transactional
    public DefaultRateEntity closeRate(UUID uuid) {
        DefaultRateEntity rate = findRateOrThrow(uuid);
        rate.setEffectiveTo(LocalDate.now());
        return save(rate);
    }

    /**
     * Create a new rate effective from a specific date, closing the previous one
     */
    @Transactional
    public DefaultRateEntity createNewRateVersion(UUID activityTypeUuid, BigDecimal newRatePerHour,
                                                 LocalDate effectiveFrom) {
        // Close any existing active rates for this activity type
        List<DefaultRateEntity> activeRates = defaultRateRepository.findActiveRates(effectiveFrom);
        for (DefaultRateEntity activeRate : activeRates) {
            if (activeRate.getActivityType().getUuid().equals(activityTypeUuid)) {
                activeRate.setEffectiveTo(effectiveFrom.minusDays(1));
                save(activeRate);
            }
        }

        // Create new rate
        return createRate(activityTypeUuid, newRatePerHour, effectiveFrom, null);
    }
}
