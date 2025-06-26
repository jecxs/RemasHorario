package com.pontificia.remashorario.modules.learningSpace;

import com.pontificia.remashorario.modules.TimeSlot.TimeSlotEntity;
import com.pontificia.remashorario.modules.classSession.ClassSessionEntity;
import com.pontificia.remashorario.modules.classSession.ClassSessionRepository;
import com.pontificia.remashorario.modules.course.CourseEntity;
import com.pontificia.remashorario.modules.course.CourseService;
import com.pontificia.remashorario.modules.learningSpace.dto.LearningSpaceRequestDTO;
import com.pontificia.remashorario.modules.learningSpace.dto.LearningSpaceResponseDTO;
import com.pontificia.remashorario.modules.learningSpace.mapper.LearningSpaceMapper;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.modules.TimeSlot.TimeSlotService;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LearningSpaceService extends BaseService<LearningSpaceEntity> {
    private final LearningSpaceMapper learningSpaceMapper;
    private final LearningSpaceRepository learningSpaceRepository;
    private final CourseService courseService;
    private final TimeSlotService timeSlotService;
    private final ClassSessionRepository classSessionRepository;

    public LearningSpaceService(LearningSpaceRepository learningSpaceRepository,
                               LearningSpaceMapper learningSpaceMapper,
                               CourseService courseService,
                               TimeSlotService timeSlotService,
                               ClassSessionRepository classSessionRepository) {
        super(learningSpaceRepository);
        this.learningSpaceMapper = learningSpaceMapper;
        this.learningSpaceRepository = learningSpaceRepository;
        this.courseService = courseService;
        this.timeSlotService = timeSlotService;
        this.classSessionRepository = classSessionRepository;
    }

    public List<LearningSpaceResponseDTO> getEligibleSpaces(UUID courseUuid, String dayOfWeek, UUID timeSlotUuid) {
        CourseEntity course = courseService.findCourseOrThrow(courseUuid);

        // Determinar tipo de enseñanza requerido
        TeachingTypeEntity.ETeachingType requiredType = course.getWeeklyPracticeHours() > 0 ?
                TeachingTypeEntity.ETeachingType.PRACTICE : TeachingTypeEntity.ETeachingType.THEORY;

        List<LearningSpaceEntity> eligibleSpaces = learningSpaceRepository
                .findByTypeUUID_Name(requiredType);

        // Si el curso tiene especialidad preferida, priorizar esas aulas
        if (course.getPreferredSpecialty() != null) {
            List<LearningSpaceEntity> preferredSpaces = eligibleSpaces.stream()
                    .filter(space -> space.getSpecialty() != null &&
                            space.getSpecialty().getUuid().equals(course.getPreferredSpecialty().getUuid()))
                    .collect(Collectors.toList());

            if (!preferredSpaces.isEmpty()) {
                eligibleSpaces = preferredSpaces;
            }
        }

        // Filtrar por disponibilidad si se especifica día y turno
        if (dayOfWeek != null && timeSlotUuid != null) {
            TimeSlotEntity timeSlot = timeSlotService.findOrThrow(timeSlotUuid);
            eligibleSpaces = eligibleSpaces.stream()
                    .filter(space -> isSpaceAvailableInTimeSlot(space, dayOfWeek, timeSlot))
                    .collect(Collectors.toList());
        }

        return learningSpaceMapper.toResponseDTOList(eligibleSpaces);
    }

    public List<LearningSpaceEntity> getSpacesByTeachingType(String teachingTypeName) {
        TeachingTypeEntity.ETeachingType type = TeachingTypeEntity.ETeachingType.valueOf(teachingTypeName);
        return learningSpaceRepository.findByTypeUUID_Name(type);
    }




    // Sobrecarga del método existente para mantener compatibilidad
    public List<LearningSpaceResponseDTO> getEligibleSpacesForSpecificHours(
            UUID courseUuid, String dayOfWeek, List<String> teachingHourUuids) {
        return getEligibleSpacesForSpecificHours(courseUuid, dayOfWeek, teachingHourUuids, null);
    }

    // ✅ MÉTODO PRINCIPAL MEJORADO
    public List<LearningSpaceResponseDTO> getEligibleSpacesForSpecificHours(
            UUID courseUuid, String dayOfWeek, List<String> teachingHourUuids, String sessionType) {

        System.out.println("=== GET ELIGIBLE SPACES FOR SPECIFIC HOURS (IMPROVED) ===");
        System.out.println("Course UUID: " + courseUuid);
        System.out.println("Day: " + dayOfWeek);
        System.out.println("Teaching Hour UUIDs: " + teachingHourUuids);
        System.out.println("Session Type: " + sessionType);

        List<LearningSpaceEntity> allSpaces;

        // ✅ Si se especifica un sessionType, filtrar por ese tipo
        if (sessionType != null && !sessionType.trim().isEmpty()) {
            try {
                TeachingTypeEntity.ETeachingType requiredType =
                        TeachingTypeEntity.ETeachingType.valueOf(sessionType.toUpperCase());

                System.out.println("Filtering by session type: " + requiredType);
                allSpaces = learningSpaceRepository.findByTypeUUID_Name(requiredType);

            } catch (IllegalArgumentException e) {
                System.out.println("Invalid session type: " + sessionType + ", using all spaces");
                allSpaces = learningSpaceRepository.findAll();
            }
        } else {
            // ✅ Si no se especifica sessionType, devolver todos los espacios
            System.out.println("No session type specified, returning all spaces");
            allSpaces = learningSpaceRepository.findAll();
        }

        System.out.println("Spaces to check: " + allSpaces.size());

        // Filtrar por disponibilidad en horas específicas
        List<LearningSpaceEntity> availableSpaces = allSpaces.stream()
                .filter(space -> {
                    boolean available = isSpaceAvailableForSpecificHours(
                            space.getUuid(), dayOfWeek, teachingHourUuids);

                    System.out.println("Space " + space.getName() +
                            " (" + space.getTypeUUID().getName() + "): " +
                            (available ? "AVAILABLE" : "OCCUPIED"));

                    return available;
                })
                .collect(Collectors.toList());

        System.out.println("Final available spaces: " + availableSpaces.size());

        // ✅ Mostrar desglose por tipo solo si no se filtró previamente
        if (sessionType == null || sessionType.trim().isEmpty()) {
            long theorySpaces = availableSpaces.stream()
                    .filter(space -> space.getTypeUUID().getName() == TeachingTypeEntity.ETeachingType.THEORY)
                    .count();
            long practiceSpaces = availableSpaces.stream()
                    .filter(space -> space.getTypeUUID().getName() == TeachingTypeEntity.ETeachingType.PRACTICE)
                    .count();

            System.out.println("  - THEORY spaces available: " + theorySpaces);
            System.out.println("  - PRACTICE spaces available: " + practiceSpaces);
        }

        return learningSpaceMapper.toResponseDTOList(availableSpaces);
    }
    // ✅ Método auxiliar para verificar disponibilidad de horas específicas
    public boolean isSpaceAvailableForSpecificHours(UUID spaceUuid, String dayOfWeek, List<String> teachingHourUuids) {
        List<ClassSessionEntity> conflicts = classSessionRepository
                .findByLearningSpaceAndDayOfWeekAndSpecificHours(
                        spaceUuid,
                        dayOfWeek.toUpperCase(),
                        teachingHourUuids);
        if (!conflicts.isEmpty()) {
            conflicts.forEach(conflict -> {
            });
        }
        boolean isAvailable = conflicts.isEmpty();
        return isAvailable;
    }


    private boolean isSpaceAvailableInTimeSlot(LearningSpaceEntity space, String dayOfWeek, TimeSlotEntity timeSlot) {
        System.out.println("=== DEBUG SPACE AVAILABILITY (DETAILED) ===");
        System.out.println("Checking space: " + space.getName());
        System.out.println("Day: " + dayOfWeek);
        System.out.println("TimeSlot: " + timeSlot.getName() + " (" + timeSlot.getStartTime() + " - " + timeSlot.getEndTime() + ")");

        // Usar el método original que ya tienes
        List<ClassSessionEntity> occupiedSessions = classSessionRepository
                .findByLearningSpaceAndDayOfWeekAndTimeSlotOverlap(
                        space.getUuid(),
                        dayOfWeek.toUpperCase(),
                        timeSlot.getStartTime().toString(),
                        timeSlot.getEndTime().toString());

        System.out.println("Occupied sessions found: " + occupiedSessions.size());

        if (occupiedSessions.isEmpty()) {
        } else {
            occupiedSessions.forEach((session) -> {
                System.out.println("  📚 Session Details:");
                System.out.println("    - Course: " + session.getCourse().getName());
                System.out.println("    - Teacher: " + session.getTeacher().getFullName());
                System.out.println("    - Group: " + session.getStudentGroup().getName());
                System.out.println("    - Day: " + session.getDayOfWeek());

                System.out.println("    - Teaching Hours in this session:");
                session.getTeachingHours().forEach(hour -> {
                    System.out.println("      ⏰ Hour " + hour.getOrderInTimeSlot() +
                            ": " + hour.getStartTime() + " - " + hour.getEndTime() +
                            " (Duration: " + hour.getDurationMinutes() + " min)");

                    // Verificar si esta hora específica se solapa con el turno solicitado
                    boolean startOverlaps = hour.getStartTime().isBefore(timeSlot.getEndTime()) &&
                            hour.getStartTime().isAfter(timeSlot.getStartTime().minusMinutes(1));
                    boolean endOverlaps = hour.getEndTime().isAfter(timeSlot.getStartTime()) &&
                            hour.getEndTime().isBefore(timeSlot.getEndTime().plusMinutes(1));
                    boolean contains = hour.getStartTime().compareTo(timeSlot.getStartTime()) >= 0 &&
                            hour.getEndTime().compareTo(timeSlot.getEndTime()) <= 0;
                    boolean surrounds = hour.getStartTime().isBefore(timeSlot.getStartTime()) &&
                            hour.getEndTime().isAfter(timeSlot.getEndTime());

                    boolean anyOverlap = startOverlaps || endOverlaps || contains || surrounds;

                    System.out.println("        ▶️ Overlaps with requested timeslot: " + anyOverlap);
                    if (anyOverlap) {
                        System.out.println("        🔍 Overlap analysis:");
                        System.out.println("          - Start overlaps: " + startOverlaps);
                        System.out.println("          - End overlaps: " + endOverlaps);
                        System.out.println("          - Contains: " + contains);
                        System.out.println("          - Surrounds: " + surrounds);
                    }
                });

                // Obtener información del turno de la sesión existente
                if (!session.getTeachingHours().isEmpty()) {
                    var firstHour = session.getTeachingHours().iterator().next();
                    var sessionTimeSlot = firstHour.getTimeSlot();
                    System.out.println("    - Session's TimeSlot: " + sessionTimeSlot.getName() +
                            " (" + sessionTimeSlot.getStartTime() + " - " + sessionTimeSlot.getEndTime() + ")");

                    boolean sameTimeSlot = sessionTimeSlot.getUuid().equals(timeSlot.getUuid());
                    System.out.println("    - Same TimeSlot as requested: " + sameTimeSlot);
                }
            });
        }

        boolean isAvailable = occupiedSessions.isEmpty();
        return isAvailable;
    }

    /**
     * Obtiene todos los espacios de aprendizaje y los convierte a un formato de respuesta (DTO).
     *
     * @return Lista de DTOs de respuesta de espacios de aprendizaje.
     */
    public List<LearningSpaceResponseDTO> getAllLearningSpaces() {
        List<LearningSpaceEntity> modalities = findAll();
        return learningSpaceMapper.toResponseDTOList(modalities);
    }

    /**
     * Crea un nuevo espacio de aprendizaje con los datos proporcionados en el DTO.
     *
     * @param requestDTO DTO con los datos necesarios para crear el espacio de aprendizaje.
     * @return DTO de respuesta con los detalles del espacio de aprendizaje creado.
     */
    @Transactional
    public LearningSpaceResponseDTO createLearningSpace(LearningSpaceRequestDTO requestDTO) {
        LearningSpaceEntity modality = learningSpaceMapper.toEntity(requestDTO);
        LearningSpaceEntity savedModality = save(modality);

        return learningSpaceMapper.toResponseDTO(savedModality);
    }

    /**
     * Actualiza un espacio de aprendizaje existente con los datos proporcionados en el DTO.
     *
     * @param uuid       UUID del espacio de aprendizaje a actualizar.
     * @param requestDTO DTO con los nuevos datos del espacio de aprendizaje.
     * @return DTO de respuesta con los detalles del espacio de aprendizaje actualizado.
     */
    @Transactional
    public LearningSpaceResponseDTO updateLearningSpace(UUID uuid, LearningSpaceRequestDTO requestDTO) {
        LearningSpaceEntity modality = findOrThrow(uuid);

        learningSpaceMapper.updateEntityFromDTO(requestDTO, modality);
        LearningSpaceEntity updatedModality = update(modality);

        return learningSpaceMapper.toResponseDTO(updatedModality);
    }

    /**
     * Elimina un espacio de aprendizaje dado su UUID.
     *
     * @param uuid UUID del espacio de aprendizaje a eliminar.
     */
    @Transactional
    public void deleteLearningSpace(UUID uuid) {
        findOrThrow(uuid);
        deleteById(uuid);
    }


    public List<LearningSpaceResponseDTO> findByTeachingType(TeachingTypeEntity.ETeachingType tipo) {
        List<LearningSpaceEntity> espacios = learningSpaceRepository.findByTypeUUID_Name(tipo);
        return learningSpaceMapper.toResponseDTOList(espacios);
    }

    public List<LearningSpaceResponseDTO> findByCapacityMinima(int capacidad) {
        List<LearningSpaceEntity> espacios = learningSpaceRepository.findByCapacityGreaterThanEqual(capacidad);
        return learningSpaceMapper.toResponseDTOList(espacios);
    }

    public List<LearningSpaceResponseDTO> findByTipoAndCapacityMinima(TeachingTypeEntity.ETeachingType tipo, int capacidad) {
        List<LearningSpaceEntity> espacios = learningSpaceRepository.findByTypeUUID_NameAndCapacityGreaterThanEqual(tipo, capacidad);
        return learningSpaceMapper.toResponseDTOList(espacios);
    }

    public List<LearningSpaceEntity> findEntitiesByTypeAndSpecialty(TeachingTypeEntity.ETeachingType tipo, java.util.UUID specialtyUuid) {
        if (specialtyUuid == null) {
            return learningSpaceRepository.findByTypeUUID_NameAndSpecialtyIsNull(tipo);
        }
        return learningSpaceRepository.findByTypeUUID_NameAndSpecialty_Uuid(tipo, specialtyUuid);
    }

    public boolean existsByName(String name) {
        return learningSpaceRepository.existsByName(name);
    }

}
