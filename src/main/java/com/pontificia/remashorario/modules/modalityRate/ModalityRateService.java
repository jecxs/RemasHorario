package com.pontificia.remashorario.modules.modalityRate;

import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeEntity;
import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeService;
import com.pontificia.remashorario.modules.educationalModality.EducationalModalityEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing hourly rates by educational modality
 * Different modalities (Instituto, Escuela) can have different rates
 */
@Service
public class ModalityRateService extends BaseService<ModalityRateEntity> {

    private final ModalityRateRepository modalityRateRepository;
    private final AttendanceActivityTypeService activityTypeService;

    @Autowired
    public ModalityRateService(ModalityRateRepository modalityRateRepository,
                              AttendanceActivityTypeService activityTypeService) {
        super(modalityRateRepository);
        this.modalityRateRepository = modalityRateRepository;
        this.activityTypeService = activityTypeService;
    }

    public List<ModalityRateEntity> getAllRates() {
        return findAll();
    }

    public ModalityRateEntity getRateById(UUID uuid) {
        return findRateOrThrow(uuid);
    }

    public ModalityRateEntity getRateByIdWithDetails(UUID uuid) {
        return modalityRateRepository.findByIdWithDetails(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Tarifa de modalidad no encontrada con ID: " + uuid));
    }

    public List<ModalityRateEntity> getRatesByModality(UUID modalityUuid) {
        return modalityRateRepository.findByModalityUuid(modalityUuid);
    }

    public List<ModalityRateEntity> getRatesByActivityType(UUID activityTypeUuid) {
        return modalityRateRepository.findByActivityTypeUuid(activityTypeUuid);
    }

    /**
     * Get the active rate for a specific modality and activity type on a given date
     */
    public ModalityRateEntity getActiveRateByModalityAndActivityType(UUID modalityUuid,
                                                                     UUID activityTypeUuid,
                                                                     LocalDate date) {
        return modalityRateRepository.findActiveRateByModalityAndActivityType(modalityUuid, activityTypeUuid, date)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró tarifa activa para la modalidad y tipo de actividad en la fecha: " + date));
    }

    /**
     * Get all active rates for a specific modality on a given date
     */
    public List<ModalityRateEntity> getActiveRatesByModality(UUID modalityUuid, LocalDate date) {
        return modalityRateRepository.findActiveRatesByModality(modalityUuid, date);
    }

    public ModalityRateEntity findRateOrThrow(UUID uuid) {
        return findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Tarifa de modalidad no encontrada con ID: " + uuid));
    }

    @Transactional
    public ModalityRateEntity createRate(UUID modalityUuid, UUID activityTypeUuid,
                                        BigDecimal ratePerHour, LocalDate effectiveFrom,
                                        LocalDate effectiveTo) {
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

        // Get modality entity (we'll need to inject EducationalModalityService if needed)
        EducationalModalityEntity modality = new EducationalModalityEntity();
        modality.setUuid(modalityUuid);

        ModalityRateEntity rate = new ModalityRateEntity();
        rate.setModality(modality);
        rate.setActivityType(activityType);
        rate.setRatePerHour(ratePerHour);
        rate.setEffectiveFrom(effectiveFrom);
        rate.setEffectiveTo(effectiveTo);

        return save(rate);
    }

    @Transactional
    public ModalityRateEntity updateRate(UUID uuid, BigDecimal ratePerHour,
                                        LocalDate effectiveFrom, LocalDate effectiveTo) {
        ModalityRateEntity rate = findRateOrThrow(uuid);

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
        ModalityRateEntity rate = findRateOrThrow(uuid);
        // TODO: Validate no payrolls are using this rate
        deleteById(uuid);
    }

    /**
     * Get the applicable rate per minute for calculations
     */
    public BigDecimal getRatePerMinute(UUID modalityUuid, UUID activityTypeUuid, LocalDate date) {
        ModalityRateEntity rate = getActiveRateByModalityAndActivityType(modalityUuid, activityTypeUuid, date);
        return rate.getRatePerHour().divide(BigDecimal.valueOf(60), 4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Close a rate by setting its effectiveTo date to today
     */
    @Transactional
    public ModalityRateEntity closeRate(UUID uuid) {
        ModalityRateEntity rate = findRateOrThrow(uuid);
        rate.setEffectiveTo(LocalDate.now());
        return save(rate);
    }

    /**
     * Create a new rate effective from a specific date, closing the previous one
     */
    @Transactional
    public ModalityRateEntity createNewRateVersion(UUID modalityUuid, UUID activityTypeUuid,
                                                  BigDecimal newRatePerHour, LocalDate effectiveFrom) {
        // Close any existing active rates for this modality and activity type
        List<ModalityRateEntity> activeRates = modalityRateRepository.findActiveRatesByModality(modalityUuid, effectiveFrom);
        for (ModalityRateEntity activeRate : activeRates) {
            if (activeRate.getActivityType().getUuid().equals(activityTypeUuid)) {
                activeRate.setEffectiveTo(effectiveFrom.minusDays(1));
                save(activeRate);
            }
        }

        // Create new rate
        return createRate(modalityUuid, activityTypeUuid, newRatePerHour, effectiveFrom, null);
    }
}
