package com.pontificia.remashorario.modules.attendanceActivityType;

import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing attendance activity types
 * Examples: Regular Class, Workshop, Substitute Exam, Extra Activity
 */
@Service
public class AttendanceActivityTypeService extends BaseService<AttendanceActivityTypeEntity> {

    private final AttendanceActivityTypeRepository activityTypeRepository;

    @Autowired
    public AttendanceActivityTypeService(AttendanceActivityTypeRepository activityTypeRepository) {
        super(activityTypeRepository);
        this.activityTypeRepository = activityTypeRepository;
    }

    public List<AttendanceActivityTypeEntity> getAllActivityTypes() {
        return findAll();
    }

    public AttendanceActivityTypeEntity getActivityTypeById(UUID uuid) {
        return findActivityTypeOrThrow(uuid);
    }

    public AttendanceActivityTypeEntity getActivityTypeByCode(String code) {
        return activityTypeRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de actividad no encontrado con código: " + code));
    }

    public AttendanceActivityTypeEntity findActivityTypeOrThrow(UUID uuid) {
        return findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de actividad no encontrado con ID: " + uuid));
    }

    @Transactional
    public AttendanceActivityTypeEntity createActivityType(String code, String name, String description) {
        // Validate unique code
        if (activityTypeRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Ya existe un tipo de actividad con el código: " + code);
        }

        AttendanceActivityTypeEntity activityType = new AttendanceActivityTypeEntity();
        activityType.setCode(code);
        activityType.setName(name);
        activityType.setDescription(description);

        return save(activityType);
    }

    @Transactional
    public AttendanceActivityTypeEntity updateActivityType(UUID uuid, String code, String name, String description) {
        AttendanceActivityTypeEntity activityType = findActivityTypeOrThrow(uuid);

        // Validate unique code if it changes
        if (!activityType.getCode().equals(code) && activityTypeRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Ya existe un tipo de actividad con el código: " + code);
        }

        activityType.setCode(code);
        activityType.setName(name);
        activityType.setDescription(description);

        return save(activityType);
    }

    @Transactional
    public void deleteActivityType(UUID uuid) {
        AttendanceActivityTypeEntity activityType = findActivityTypeOrThrow(uuid);
        // TODO: Validate no attendances or rates are using this activity type
        deleteById(uuid);
    }

    /**
     * Helper method to create default activity types for initial setup
     */
    @Transactional
    public void createDefaultActivityTypes() {
        if (activityTypeRepository.count() == 0) {
            createActivityType("REGULAR_CLASS", "Clase Regular", "Clase programada en el horario");
            createActivityType("WORKSHOP", "Taller", "Taller o actividad extra curricular");
            createActivityType("SUBSTITUTE_EXAM", "Examen Sustitutorio", "Supervisión de examen de recuperación");
            createActivityType("EXTRA_HOURS", "Horas Extras", "Horas adicionales no programadas");
        }
    }
}
