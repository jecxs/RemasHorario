package com.pontificia.remashorario.modules.teacherRate;

import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeEntity;
import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeService;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teacher.TeacherService;
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
 * Service for managing teacher-specific hourly rates
 * These rates override modality and default rates for specific teachers
 */
@Service
public class TeacherRateService extends BaseService<TeacherRateEntity> {

    private final TeacherRateRepository teacherRateRepository;
    private final TeacherService teacherService;
    private final AttendanceActivityTypeService activityTypeService;

    @Autowired
    public TeacherRateService(TeacherRateRepository teacherRateRepository,
                             TeacherService teacherService,
                             AttendanceActivityTypeService activityTypeService) {
        super(teacherRateRepository);
        this.teacherRateRepository = teacherRateRepository;
        this.teacherService = teacherService;
        this.activityTypeService = activityTypeService;
    }

    public List<TeacherRateEntity> getAllRates() {
        return findAll();
    }

    public TeacherRateEntity getRateById(UUID uuid) {
        return findRateOrThrow(uuid);
    }

    public TeacherRateEntity getRateByIdWithDetails(UUID uuid) {
        return teacherRateRepository.findByIdWithDetails(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Tarifa de docente no encontrada con ID: " + uuid));
    }

    public List<TeacherRateEntity> getRatesByTeacher(UUID teacherUuid) {
        return teacherRateRepository.findByTeacherUuid(teacherUuid);
    }

    public List<TeacherRateEntity> getRatesByActivityType(UUID activityTypeUuid) {
        return teacherRateRepository.findByActivityTypeUuid(activityTypeUuid);
    }

    /**
     * Get the active rate for a specific teacher and activity type on a given date
     */
    public TeacherRateEntity getActiveRateByTeacherAndActivityType(UUID teacherUuid,
                                                                   UUID activityTypeUuid,
                                                                   LocalDate date) {
        return teacherRateRepository.findActiveRateByTeacherAndActivityType(teacherUuid, activityTypeUuid, date)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró tarifa activa para el docente y tipo de actividad en la fecha: " + date));
    }

    /**
     * Get all active rates for a specific teacher on a given date
     */
    public List<TeacherRateEntity> getActiveRatesByTeacher(UUID teacherUuid, LocalDate date) {
        return teacherRateRepository.findActiveRatesByTeacher(teacherUuid, date);
    }

    /**
     * Check if a teacher has a specific rate for an activity type
     */
    public boolean hasSpecificRate(UUID teacherUuid, UUID activityTypeUuid, LocalDate date) {
        return teacherRateRepository.findActiveRateByTeacherAndActivityType(teacherUuid, activityTypeUuid, date)
                .isPresent();
    }

    public TeacherRateEntity findRateOrThrow(UUID uuid) {
        return findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Tarifa de docente no encontrada con ID: " + uuid));
    }

    @Transactional
    public TeacherRateEntity createRate(UUID teacherUuid, UUID activityTypeUuid,
                                       BigDecimal ratePerHour, LocalDate effectiveFrom,
                                       LocalDate effectiveTo) {
        // Validate teacher exists
        TeacherEntity teacher = teacherService.findTeacherOrThrow(teacherUuid);

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

        TeacherRateEntity rate = new TeacherRateEntity();
        rate.setTeacher(teacher);
        rate.setActivityType(activityType);
        rate.setRatePerHour(ratePerHour);
        rate.setEffectiveFrom(effectiveFrom);
        rate.setEffectiveTo(effectiveTo);

        return save(rate);
    }

    @Transactional
    public TeacherRateEntity updateRate(UUID uuid, BigDecimal ratePerHour,
                                       LocalDate effectiveFrom, LocalDate effectiveTo) {
        TeacherRateEntity rate = findRateOrThrow(uuid);

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
        TeacherRateEntity rate = findRateOrThrow(uuid);
        // TODO: Validate no payrolls are using this rate
        deleteById(uuid);
    }

    /**
     * Get the applicable rate per minute for calculations
     */
    public BigDecimal getRatePerMinute(UUID teacherUuid, UUID activityTypeUuid, LocalDate date) {
        TeacherRateEntity rate = getActiveRateByTeacherAndActivityType(teacherUuid, activityTypeUuid, date);
        return rate.getRatePerHour().divide(BigDecimal.valueOf(60), 4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Close a rate by setting its effectiveTo date to today
     */
    @Transactional
    public TeacherRateEntity closeRate(UUID uuid) {
        TeacherRateEntity rate = findRateOrThrow(uuid);
        rate.setEffectiveTo(LocalDate.now());
        return save(rate);
    }

    /**
     * Create a new rate effective from a specific date, closing the previous one
     */
    @Transactional
    public TeacherRateEntity createNewRateVersion(UUID teacherUuid, UUID activityTypeUuid,
                                                 BigDecimal newRatePerHour, LocalDate effectiveFrom) {
        // Close any existing active rates for this teacher and activity type
        List<TeacherRateEntity> activeRates = teacherRateRepository.findActiveRatesByTeacher(teacherUuid, effectiveFrom);
        for (TeacherRateEntity activeRate : activeRates) {
            if (activeRate.getActivityType().getUuid().equals(activityTypeUuid)) {
                activeRate.setEffectiveTo(effectiveFrom.minusDays(1));
                save(activeRate);
            }
        }

        // Create new rate
        return createRate(teacherUuid, activityTypeUuid, newRatePerHour, effectiveFrom, null);
    }

    /**
     * Bulk create rates for a teacher across multiple activity types
     */
    @Transactional
    public List<TeacherRateEntity> createBulkRatesForTeacher(UUID teacherUuid,
                                                            List<TeacherRateEntity> rates,
                                                            LocalDate effectiveFrom) {
        // Validate teacher
        teacherService.findTeacherOrThrow(teacherUuid);

        for (TeacherRateEntity rate : rates) {
            rate.setEffectiveFrom(effectiveFrom);
            // Validate and set teacher
            TeacherEntity teacher = new TeacherEntity();
            teacher.setUuid(teacherUuid);
            rate.setTeacher(teacher);
        }

        return saveAll(rates);
    }
}
