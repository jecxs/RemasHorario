package com.pontificia.remashorario.modules.teacherAvailability;

import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teacher.TeacherRepository;
import com.pontificia.remashorario.modules.teacher.TeacherService;
import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherAvailabilityRequestDTO;
import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherAvailabilityResponseDTO;
import com.pontificia.remashorario.modules.teacherAvailability.mapper.TeacherAvailabilityMapper;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TeacherAvailabilityService extends BaseService<TeacherAvailabilityEntity> {

    private final TeacherAvailabilityRepository availabilityRepository;
    private final TeacherAvailabilityMapper availabilityMapper;
    private final TeacherService teacherService;

    @Autowired
    public TeacherAvailabilityService(TeacherAvailabilityRepository availabilityRepository,
                                      TeacherAvailabilityMapper availabilityMapper,
                                      TeacherService teacherService) {
        super(availabilityRepository);
        this.availabilityRepository = availabilityRepository;
        this.availabilityMapper = availabilityMapper;
        this.teacherService = teacherService;
    }

    public List<TeacherAvailabilityResponseDTO> getTeacherAvailabilities(UUID teacherUuid) {
        List<TeacherAvailabilityEntity> availabilities = availabilityRepository.findByTeacherUuid(teacherUuid);
        return availabilityMapper.toResponseDTOList(availabilities);
    }

    public List<TeacherAvailabilityResponseDTO> getTeacherAvailabilitiesByDay(UUID teacherUuid, DayOfWeek dayOfWeek) {
        List<TeacherAvailabilityEntity> availabilities =
                availabilityRepository.findByTeacherUuidAndDayOfWeek(teacherUuid, dayOfWeek);
        return availabilityMapper.toResponseDTOList(availabilities);
    }

    @Transactional
    public TeacherAvailabilityResponseDTO createAvailability(UUID teacherUuid, TeacherAvailabilityRequestDTO dto) {
        TeacherEntity teacher = teacherService.findTeacherOrThrow(teacherUuid);

        // Validar horario
        validateAvailabilityTimes(dto);

        // Verificar solapamientos - convertir parámetros a String para SQL Server
        List<TeacherAvailabilityEntity> overlapping = availabilityRepository.findOverlapping(
                teacherUuid, dto.getDayOfWeek().name(),
                dto.getStartTime().toString(), dto.getEndTime().toString());

        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("El horario se solapa con una disponibilidad existente");
        }

        // Crear y guardar
        TeacherAvailabilityEntity availability = availabilityMapper.toEntity(dto, teacher);
        TeacherAvailabilityEntity saved = save(availability);

        return availabilityMapper.toResponseDTO(saved);
    }

    @Transactional
    public TeacherAvailabilityResponseDTO updateAvailability(UUID uuid, TeacherAvailabilityRequestDTO dto) {
        TeacherAvailabilityEntity availability = findAvailabilityOrThrow(uuid);

        // Validar horario
        validateAvailabilityTimes(dto);

        // Verificar solapamientos (excluyendo la disponibilidad actual)
        List<TeacherAvailabilityEntity> overlapping = availabilityRepository.findOverlapping(
                availability.getTeacher().getUuid(), dto.getDayOfWeek().name(),
                dto.getStartTime().toString(), dto.getEndTime().toString());

        overlapping.removeIf(a -> a.getUuid().equals(uuid));

        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("El horario se solapa con una disponibilidad existente");
        }

        // TODO: Verificar si hay asignaciones que dependan de esta disponibilidad

        // Actualizar
        availabilityMapper.updateEntityFromDTO(availability, dto);
        TeacherAvailabilityEntity updated = save(availability);

        return availabilityMapper.toResponseDTO(updated);
    }

    @Transactional
    public void deleteAvailability(UUID uuid) {
        TeacherAvailabilityEntity availability = findAvailabilityOrThrow(uuid);

        // TODO: Verificar si hay asignaciones que dependan de esta disponibilidad

        deleteById(uuid);
    }

    @Transactional
    public void deleteAllTeacherAvailabilities(UUID teacherUuid) {
        // Verificar que exista el docente
        teacherService.findTeacherOrThrow(teacherUuid);

        // TODO: Verificar si hay asignaciones antes de eliminar

        availabilityRepository.deleteByTeacherUuid(teacherUuid);
    }

    /**
     * Verifica si un docente está disponible en un horario específico
     */
    public boolean isTeacherAvailable(UUID teacherUuid, DayOfWeek dayOfWeek,
                                      LocalTime startTime, LocalTime endTime) {
        List<TeacherAvailabilityEntity> containing = availabilityRepository.findContaining(
                teacherUuid, dayOfWeek.name(), startTime.toString(), endTime.toString());
        return !containing.isEmpty();
    }

    private TeacherAvailabilityEntity findAvailabilityOrThrow(UUID uuid) {
        return findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Disponibilidad no encontrada con ID: " + uuid));
    }

    private void validateAvailabilityTimes(TeacherAvailabilityRequestDTO dto) {
        if (dto.getEndTime().isBefore(dto.getStartTime()) || dto.getEndTime().equals(dto.getStartTime())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // Validar horario laboral (6:00 AM - 10:00 PM)
        LocalTime minTime = LocalTime.of(6, 0);
        LocalTime maxTime = LocalTime.of(22, 0);

        if (dto.getStartTime().isBefore(minTime) || dto.getEndTime().isAfter(maxTime)) {
            throw new IllegalArgumentException("El horario debe estar entre las 6:00 AM y 10:00 PM");
        }
    }
}