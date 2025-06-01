package com.pontificia.remashorario.modules.classSession;

import com.pontificia.remashorario.modules.classSession.dto.ClassSessionFilterDTO;
import com.pontificia.remashorario.modules.classSession.dto.ClassSessionRequestDTO;
import com.pontificia.remashorario.modules.classSession.dto.ClassSessionResponseDTO;
import com.pontificia.remashorario.modules.classSession.mapper.ClassSessionMapper;
import com.pontificia.remashorario.modules.course.CourseEntity;
import com.pontificia.remashorario.modules.course.CourseService;
import com.pontificia.remashorario.modules.learningSpace.LearningSpaceEntity;
import com.pontificia.remashorario.modules.learningSpace.LearningSpaceService;
import com.pontificia.remashorario.modules.studentGroup.StudentGroupEntity;
import com.pontificia.remashorario.modules.studentGroup.StudentGroupService;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teacher.TeacherService;
import com.pontificia.remashorario.modules.teacherAvailability.TeacherAvailabilityService;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourRepository;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeService;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClassSessionService extends BaseService<ClassSessionEntity> {

    private final ClassSessionRepository classSessionRepository;
    private final ClassSessionMapper classSessionMapper;
    private final StudentGroupService studentGroupService;
    private final CourseService courseService;
    private final TeacherService teacherService;
    private final LearningSpaceService learningSpaceService;
    private final TeachingTypeService teachingTypeService;
    private final TeacherAvailabilityService teacherAvailabilityService;
    private final TeachingHourRepository teachingHourRepository;

    @Autowired
    public ClassSessionService(ClassSessionRepository classSessionRepository,
                               ClassSessionMapper classSessionMapper,
                               StudentGroupService studentGroupService,
                               CourseService courseService,
                               TeacherService teacherService,
                               LearningSpaceService learningSpaceService,
                               TeachingTypeService teachingTypeService,
                               TeacherAvailabilityService teacherAvailabilityService,
                               TeachingHourRepository teachingHourRepository) {
        super(classSessionRepository);
        this.classSessionRepository = classSessionRepository;
        this.classSessionMapper = classSessionMapper;
        this.studentGroupService = studentGroupService;
        this.courseService = courseService;
        this.teacherService = teacherService;
        this.learningSpaceService = learningSpaceService;
        this.teachingTypeService = teachingTypeService;
        this.teacherAvailabilityService = teacherAvailabilityService;
        this.teachingHourRepository = teachingHourRepository;
    }

    public List<ClassSessionResponseDTO> getAllClassSessions() {
        List<ClassSessionEntity> sessions = findAll();
        return classSessionMapper.toResponseDTOList(sessions);
    }

    public ClassSessionResponseDTO getClassSessionById(UUID uuid) {
        ClassSessionEntity session = findClassSessionOrThrow(uuid);
        return classSessionMapper.toResponseDTO(session);
    }

    public ClassSessionEntity findClassSessionOrThrow(UUID uuid) {
        return findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Sesión de clase no encontrada con ID: " + uuid));
    }

    @Transactional
    public ClassSessionResponseDTO createClassSession(ClassSessionRequestDTO dto) {
        // Obtener entidades relacionadas
        StudentGroupEntity studentGroup = studentGroupService.findOrThrow(dto.getStudentGroupUuid());
        CourseEntity course = courseService.findCourseOrThrow(dto.getCourseUuid());
        TeacherEntity teacher = teacherService.findTeacherOrThrow(dto.getTeacherUuid());
        LearningSpaceEntity learningSpace = learningSpaceService.findOrThrow(dto.getLearningSpaceUuid());
        TeachingTypeEntity sessionType = teachingTypeService.findTeachingTypeOrThrow(dto.getSessionTypeUuid());

        // Validar que el curso pertenezca al mismo ciclo que el grupo
        validateCourseAndGroup(course, studentGroup);

        // Validar que el tipo de sesión sea compatible con el curso
        validateSessionTypeWithCourse(sessionType, course);

        // Validar que el aula sea compatible con el tipo de sesión
        validateLearningSpaceWithSessionType(learningSpace, sessionType);

        // Obtener y validar horas pedagógicas
        Set<TeachingHourEntity> teachingHours = getAndValidateTeachingHours(dto.getTeachingHourUuids());

        // Validar disponibilidad del docente
        validateTeacherAvailability(teacher, dto.getDayOfWeek(), teachingHours);

        // Validar conflictos
        validateNoConflicts(dto, teachingHours);

        // Crear y guardar
        ClassSessionEntity session = classSessionMapper.toEntity(
                dto, studentGroup, course, teacher, learningSpace, sessionType, teachingHours);
        session.setPeriod(studentGroup.getPeriod());
        ClassSessionEntity savedSession = save(session);

        return classSessionMapper.toResponseDTO(savedSession);
    }

    @Transactional
    public ClassSessionResponseDTO updateClassSession(UUID uuid, ClassSessionRequestDTO dto) {
        ClassSessionEntity session = findClassSessionOrThrow(uuid);

        // Obtener entidades relacionadas
        StudentGroupEntity studentGroup = studentGroupService.findOrThrow(dto.getStudentGroupUuid());
        CourseEntity course = courseService.findCourseOrThrow(dto.getCourseUuid());
        TeacherEntity teacher = teacherService.findTeacherOrThrow(dto.getTeacherUuid());
        LearningSpaceEntity learningSpace = learningSpaceService.findOrThrow(dto.getLearningSpaceUuid());
        TeachingTypeEntity sessionType = teachingTypeService.findTeachingTypeOrThrow(dto.getSessionTypeUuid());

        // Realizar las mismas validaciones que en create
        validateCourseAndGroup(course, studentGroup);
        validateSessionTypeWithCourse(sessionType, course);
        validateLearningSpaceWithSessionType(learningSpace, sessionType);

        Set<TeachingHourEntity> teachingHours = getAndValidateTeachingHours(dto.getTeachingHourUuids());
        validateTeacherAvailability(teacher, dto.getDayOfWeek(), teachingHours);
        validateNoConflicts(dto, teachingHours, uuid); // Excluir la sesión actual

        // Actualizar
        classSessionMapper.updateEntityFromDTO(
                session, dto, studentGroup, course, teacher, learningSpace, sessionType, teachingHours);
        session.setPeriod(studentGroup.getPeriod());
        ClassSessionEntity updatedSession = save(session);

        return classSessionMapper.toResponseDTO(updatedSession);
    }

    @Transactional
    public void deleteClassSession(UUID uuid) {
        ClassSessionEntity session = findClassSessionOrThrow(uuid);
        deleteById(uuid);
    }

    public List<ClassSessionResponseDTO> getSessionsByStudentGroup(UUID studentGroupUuid) {
        List<ClassSessionEntity> sessions = classSessionRepository.findByStudentGroupUuid(studentGroupUuid);
        return classSessionMapper.toResponseDTOList(sessions);
    }

    public List<ClassSessionResponseDTO> getSessionsByTeacher(UUID teacherUuid) {
        List<ClassSessionEntity> sessions = classSessionRepository.findByTeacherUuid(teacherUuid);
        return classSessionMapper.toResponseDTOList(sessions);
    }

    public List<ClassSessionResponseDTO> filterClassSessions(ClassSessionFilterDTO filters) {
        // Implementar filtros según necesidades
        // Por ahora, ejemplo básico
        if (filters.getStudentGroupUuid() != null) {
            return getSessionsByStudentGroup(filters.getStudentGroupUuid());
        } else if (filters.getTeacherUuid() != null) {
            return getSessionsByTeacher(filters.getTeacherUuid());
        }
        return getAllClassSessions();
    }

    // Métodos de validación privados
    private void validateCourseAndGroup(CourseEntity course, StudentGroupEntity studentGroup) {
        if (!course.getCycle().getUuid().equals(studentGroup.getCycle().getUuid())) {
            throw new IllegalArgumentException("El curso debe pertenecer al mismo ciclo que el grupo de estudiantes");
        }
    }

    private void validateSessionTypeWithCourse(TeachingTypeEntity sessionType, CourseEntity course) {
        boolean courseSupportsSessionType = course.getTeachingTypes().stream()
                .anyMatch(type -> type.getUuid().equals(sessionType.getUuid()));

        if (!courseSupportsSessionType) {
            throw new IllegalArgumentException("El tipo de sesión no es compatible con el curso seleccionado");
        }
    }

    private void validateLearningSpaceWithSessionType(LearningSpaceEntity learningSpace, TeachingTypeEntity sessionType) {
        if (!learningSpace.getTypeUUID().getUuid().equals(sessionType.getUuid())) {
            throw new IllegalArgumentException("El espacio de aprendizaje no es compatible con el tipo de sesión");
        }
    }

    private Set<TeachingHourEntity> getAndValidateTeachingHours(List<UUID> teachingHourUuids) {
        if (teachingHourUuids == null || teachingHourUuids.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una hora pedagógica");
        }

        Set<TeachingHourEntity> teachingHours = new HashSet<>();
        UUID timeSlotUuid = null;

        for (UUID teachingHourUuid : teachingHourUuids) {
            TeachingHourEntity teachingHour = teachingHourRepository.findById(teachingHourUuid)
                    .orElseThrow(() -> new EntityNotFoundException("Hora pedagógica no encontrada con ID: " + teachingHourUuid));

            // Validar que todas las horas pedagógicas pertenezcan al mismo turno
            if (timeSlotUuid == null) {
                timeSlotUuid = teachingHour.getTimeSlot().getUuid();
            } else if (!timeSlotUuid.equals(teachingHour.getTimeSlot().getUuid())) {
                throw new IllegalArgumentException("Todas las horas pedagógicas deben pertenecer al mismo turno");
            }

            teachingHours.add(teachingHour);
        }

        // Validar que las horas pedagógicas sean consecutivas (opcional pero recomendado)
        validateConsecutiveTeachingHours(teachingHours);

        return teachingHours;
    }

    private void validateConsecutiveTeachingHours(Set<TeachingHourEntity> teachingHours) {
        List<Integer> orders = teachingHours.stream()
                .map(TeachingHourEntity::getOrderInTimeSlot)
                .sorted()
                .collect(Collectors.toList());

        for (int i = 1; i < orders.size(); i++) {
            if (orders.get(i) != orders.get(i - 1) + 1) {
                throw new IllegalArgumentException("Las horas pedagógicas deben ser consecutivas");
            }
        }
    }

    private void validateTeacherAvailability(TeacherEntity teacher, DayOfWeek dayOfWeek, Set<TeachingHourEntity> teachingHours) {
        // Obtener el rango de tiempo total de las horas pedagógicas seleccionadas
        LocalTime startTime = teachingHours.stream()
                .map(TeachingHourEntity::getStartTime)
                .min(LocalTime::compareTo)
                .orElseThrow(() -> new IllegalArgumentException("No se pudo determinar la hora de inicio"));

        LocalTime endTime = teachingHours.stream()
                .map(TeachingHourEntity::getEndTime)
                .max(LocalTime::compareTo)
                .orElseThrow(() -> new IllegalArgumentException("No se pudo determinar la hora de fin"));

        // Verificar si el docente está disponible en ese rango de tiempo
        boolean isAvailable = teacherAvailabilityService.isTeacherAvailable(
                teacher.getUuid(), dayOfWeek, startTime, endTime);

        if (!isAvailable) {
            throw new IllegalArgumentException(
                    String.format("El docente %s no está disponible el %s de %s a %s",
                            teacher.getFullName(),
                            dayOfWeek.name(),
                            startTime.toString(),
                            endTime.toString()));
        }
    }

    private void validateNoConflicts(ClassSessionRequestDTO dto, Set<TeachingHourEntity> teachingHours) {
        validateNoConflicts(dto, teachingHours, null);
    }

    private void validateNoConflicts(ClassSessionRequestDTO dto, Set<TeachingHourEntity> teachingHours, UUID excludeSessionUuid) {
        List<UUID> teachingHourUuids = teachingHours.stream()
                .map(TeachingHourEntity::getUuid)
                .collect(Collectors.toList());

        // Verificar conflictos de docente
        List<ClassSessionEntity> teacherConflicts = classSessionRepository.findTeacherConflicts(
                dto.getTeacherUuid(), dto.getDayOfWeek(), teachingHourUuids);
        if (excludeSessionUuid != null) {
            teacherConflicts.removeIf(session -> session.getUuid().equals(excludeSessionUuid));
        }
        if (!teacherConflicts.isEmpty()) {
            throw new IllegalArgumentException("El docente ya tiene una clase asignada en ese horario");
        }

        // Verificar conflictos de aula
        List<ClassSessionEntity> spaceConflicts = classSessionRepository.findLearningSpaceConflicts(
                dto.getLearningSpaceUuid(), dto.getDayOfWeek(), teachingHourUuids);
        if (excludeSessionUuid != null) {
            spaceConflicts.removeIf(session -> session.getUuid().equals(excludeSessionUuid));
        }
        if (!spaceConflicts.isEmpty()) {
            throw new IllegalArgumentException("El aula ya está ocupada en ese horario");
        }

        // Verificar conflictos de grupo
        List<ClassSessionEntity> groupConflicts = classSessionRepository.findStudentGroupConflicts(
                dto.getStudentGroupUuid(), dto.getDayOfWeek(), teachingHourUuids);
        if (excludeSessionUuid != null) {
            groupConflicts.removeIf(session -> session.getUuid().equals(excludeSessionUuid));
        }
        if (!groupConflicts.isEmpty()) {
            throw new IllegalArgumentException("El grupo ya tiene una clase asignada en ese horario");
        }
    }
}