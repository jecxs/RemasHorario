package com.pontificia.remashorario.modules.learningSpace;

import com.pontificia.remashorario.modules.TimeSlot.TimeSlotEntity;
import com.pontificia.remashorario.modules.classSession.ClassSessionEntity;
import com.pontificia.remashorario.modules.classSession.ClassSessionRepository;
import com.pontificia.remashorario.modules.course.CourseEntity;
import com.pontificia.remashorario.modules.course.CourseService;
import com.pontificia.remashorario.modules.learningSpace.dto.LearningSpaceRequestDTO;
import com.pontificia.remashorario.modules.learningSpace.dto.LearningSpaceResponseDTO;
import com.pontificia.remashorario.modules.learningSpace.mapper.LearningSpaceMapper;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.modules.TimeSlot.TimeSlotService;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;
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


    private boolean isSpaceAvailableInTimeSlot(LearningSpaceEntity space, String dayOfWeek, TimeSlotEntity timeSlot) {
        // Verificar si el aula está ocupada en ese horario
        List<ClassSessionEntity> occupiedSessions = classSessionRepository
                .findByLearningSpaceAndDayOfWeekAndTimeSlotOverlap(
                        space.getUuid(),  // ✅ Pasar UUID en lugar de entidad
                        dayOfWeek.toUpperCase(),  // ✅ String directo
                        timeSlot.getStartTime().toString(),  // ✅ Convertir LocalTime a String
                        timeSlot.getEndTime().toString());

        return occupiedSessions.isEmpty();
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
