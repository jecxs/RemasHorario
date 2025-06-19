package com.pontificia.remashorario.modules.TimeSlot;

import com.pontificia.remashorario.modules.TimeSlot.dto.TimeSlotRequestDTO;
import com.pontificia.remashorario.modules.TimeSlot.dto.TimeSlotResponseDTO;

import com.pontificia.remashorario.modules.TimeSlot.mapper.TimeSlotMapper;
import com.pontificia.remashorario.modules.classSession.ClassSessionEntity;
import com.pontificia.remashorario.modules.classSession.ClassSessionRepository;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotMapper timeSlotMapper;
    private final TeachingHourRepository teachingHourRepository;
    private final ClassSessionRepository classSessionRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository,
                           TimeSlotMapper timeSlotMapper,
                           TeachingHourRepository teachingHourRepository,
                           ClassSessionRepository classSessionRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.timeSlotMapper = timeSlotMapper;
        this.teachingHourRepository = teachingHourRepository;
        this.classSessionRepository = classSessionRepository;
    }

    public List<TeachingHourEntity> getAvailableHours(UUID teacherUuid, UUID spaceUuid, UUID groupUuid, String dayOfWeek) {
        List<TeachingHourEntity> allHours = teachingHourRepository.findAllOrderByTimeSlotAndOrder();

        return allHours.stream()
                .filter(hour -> isHourAvailableForAssignment(hour, teacherUuid, spaceUuid, groupUuid, dayOfWeek))
                .collect(Collectors.toList());
    }

    public List<TeachingHourEntity> getHoursByTimeSlot(UUID timeSlotUuid) {
        TimeSlotEntity timeSlot = findOrThrow(timeSlotUuid);
        return teachingHourRepository.findByTimeSlotOrderByOrderInTimeSlot(timeSlot);
    }

    public List<TeachingHourEntity> getAvailableHoursByTimeSlot(UUID timeSlotUuid, String dayOfWeek) {
        List<TeachingHourEntity> hours = getHoursByTimeSlot(timeSlotUuid);

        return hours.stream()
                .filter(hour -> !isHourOccupied(hour, dayOfWeek))
                .collect(Collectors.toList());
    }

    private boolean isHourAvailableForAssignment(TeachingHourEntity hour, UUID teacherUuid, UUID spaceUuid, UUID groupUuid, String dayOfWeek) {
        // Verificar si la hora no está ocupada por el docente, aula o grupo
        List<ClassSessionEntity> conflicts = classSessionRepository.findConflicts(
                teacherUuid,
                spaceUuid,
                groupUuid,
                dayOfWeek,
                hour.getStartTime().toString(),
                hour.getEndTime().toString());

        return conflicts.isEmpty();
    }

    private boolean isHourOccupied(TeachingHourEntity hour, String dayOfWeek) {
        List<ClassSessionEntity> sessions = classSessionRepository
                .findByDayOfWeekAndTeachingHoursContaining(DayOfWeek.valueOf(dayOfWeek.toUpperCase()), hour);

        return !sessions.isEmpty();
    }


    @Transactional
    public TimeSlotResponseDTO createTimeSlot(TimeSlotRequestDTO requestDTO) {
        if (requestDTO.getStartTime().isAfter(requestDTO.getEndTime()) || requestDTO.getStartTime().equals(requestDTO.getEndTime())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin.");
        }

        long totalSlotDurationMinutes = Duration.between(requestDTO.getStartTime(), requestDTO.getEndTime()).toMinutes();
        int pedagogicalHourDuration = requestDTO.getPedagogicalHourDurationInMinutes();

        if (pedagogicalHourDuration <= 0) {
            throw new IllegalArgumentException("La duración de la hora pedagógica debe ser positiva.");
        }
        if (totalSlotDurationMinutes <= 0) {
            throw new IllegalArgumentException("La duración total del turno debe ser positiva.");
        }
        if (totalSlotDurationMinutes % pedagogicalHourDuration != 0) {
            throw new IllegalArgumentException("La duración total del turno no es un múltiplo exacto de la duración de la hora pedagógica. No se pueden encajar las horas pedagógicas sin dejar huecos.");
        }

        validateNoOverlapOrDuplicate(requestDTO.getStartTime(), requestDTO.getEndTime(), null);

        TimeSlotEntity timeSlotEntity = timeSlotMapper.toTimeSlotEntity(requestDTO);
        generateTeachingHoursForTimeSlot(timeSlotEntity, pedagogicalHourDuration, totalSlotDurationMinutes);

        TimeSlotEntity savedTimeSlot = timeSlotRepository.save(timeSlotEntity);
        return timeSlotMapper.toTimeSlotResponseDTO(savedTimeSlot);
    }

    @Transactional(readOnly = true)
    public List<TimeSlotResponseDTO> getAllTimeSlots() {
        return timeSlotRepository.findAll().stream()
                .map(timeSlotMapper::toTimeSlotResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TimeSlotResponseDTO getTimeSlotById(UUID id) {
        TimeSlotEntity timeSlotEntity = timeSlotRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Turno no encontrado con ID: " + id));
        return timeSlotMapper.toTimeSlotResponseDTO(timeSlotEntity);
    }

    @Transactional
    public TimeSlotResponseDTO updateTimeSlot(UUID id, TimeSlotRequestDTO requestDTO) {
        TimeSlotEntity existingTimeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Turno no encontrado con ID: " + id));

        if (requestDTO.getStartTime().isAfter(requestDTO.getEndTime()) || requestDTO.getStartTime().equals(requestDTO.getEndTime())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin.");
        }

        long totalSlotDurationMinutes = Duration.between(requestDTO.getStartTime(), requestDTO.getEndTime()).toMinutes();
        int pedagogicalHourDuration = requestDTO.getPedagogicalHourDurationInMinutes();

        if (pedagogicalHourDuration <= 0) {
            throw new IllegalArgumentException("La duración de la hora pedagógica debe ser positiva.");
        }
        if (totalSlotDurationMinutes <= 0) {
            throw new IllegalArgumentException("La duración total del turno debe ser positiva.");
        }
        if (totalSlotDurationMinutes % pedagogicalHourDuration != 0) {
            throw new IllegalArgumentException("La duración total del turno no es un múltiplo exacto de la duración de la hora pedagógica. No se pueden encajar las horas pedagógicas sin dejar huecos.");
        }

        validateNoOverlapOrDuplicate(requestDTO.getStartTime(), requestDTO.getEndTime(), id);

        existingTimeSlot.setName(requestDTO.getName());
        existingTimeSlot.setStartTime(requestDTO.getStartTime());
        existingTimeSlot.setEndTime(requestDTO.getEndTime());

        // Limpiar las horas pedagógicas existentes y regenerarlas
        existingTimeSlot.getTeachingHours().clear(); // Esto las marcará para eliminación por orphanRemoval
        // Es necesario llamar a save para que orphanRemoval tenga efecto antes de añadir nuevas.
        // O, si se quiere evitar un save intermedio, hay que gestionar la eliminación explícitamente o
        // asegurarse de que la colección se reemplace completamente de una manera que JPA entienda.
        // Para simplificar, aquí se limpian y luego se añaden las nuevas antes del save final.

        generateTeachingHoursForTimeSlot(existingTimeSlot, pedagogicalHourDuration, totalSlotDurationMinutes);

        TimeSlotEntity updatedTimeSlot = timeSlotRepository.save(existingTimeSlot);
        return timeSlotMapper.toTimeSlotResponseDTO(updatedTimeSlot);
    }

    private void generateTeachingHoursForTimeSlot(TimeSlotEntity timeSlotEntity, int pedagogicalHourDuration, long totalSlotDurationMinutes) {
        timeSlotEntity.getTeachingHours().clear(); // Asegurarse de que esté vacía antes de generar
        int numberOfTeachingHours = (int) (totalSlotDurationMinutes / pedagogicalHourDuration);
        LocalTime currentStartTime = timeSlotEntity.getStartTime();

        for (int i = 1; i <= numberOfTeachingHours; i++) {
            TeachingHourEntity teachingHour = new TeachingHourEntity();
            teachingHour.setOrderInTimeSlot(i);
            teachingHour.setStartTime(currentStartTime);
            LocalTime endTime = currentStartTime.plusMinutes(pedagogicalHourDuration);
            teachingHour.setEndTime(endTime);
            teachingHour.setDurationMinutes(pedagogicalHourDuration);
            // La relación bidireccional se establece al añadir a la lista del TimeSlotEntity
            timeSlotEntity.addTeachingHour(teachingHour); // Usa el helper para asegurar la relación bidireccional
            currentStartTime = endTime;
        }
    }

    private void validateNoOverlapOrDuplicate(LocalTime startTime, LocalTime endTime, UUID excludeId) {
        timeSlotRepository.findByStartTimeAndEndTime(startTime, endTime)
                .filter(ts -> excludeId == null || !ts.getUuid().equals(excludeId))
                .ifPresent(ts -> {
                    throw new IllegalArgumentException("Ya existe un turno con el mismo rango horario.");
                });

        List<TimeSlotEntity> overlapping = timeSlotRepository.findOverlapping(startTime, endTime);
        if (excludeId != null) {
            overlapping.removeIf(ts -> ts.getUuid().equals(excludeId));
        }
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("El rango horario se solapa con un turno existente.");
        }
    }

    public TimeSlotEntity findOrThrow(UUID uuid) {
        return timeSlotRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Turno no encontrado con ID: " + uuid));
    }


    @Transactional
    public void deleteTimeSlot(UUID id) {
        if (!timeSlotRepository.existsById(id)) {
            throw new EntityNotFoundException("Turno no encontrado con ID: " + id);
        }
        timeSlotRepository.deleteById(id); // orphanRemoval se encargará de las TeachingHourEntity
    }
}