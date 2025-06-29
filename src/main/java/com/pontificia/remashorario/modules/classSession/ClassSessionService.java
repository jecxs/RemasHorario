package com.pontificia.remashorario.modules.classSession;

import com.pontificia.remashorario.modules.TimeSlot.TimeSlotService;
import com.pontificia.remashorario.modules.classSession.dto.*;
import com.pontificia.remashorario.modules.classSession.mapper.ClassSessionMapper;
import com.pontificia.remashorario.modules.course.CourseEntity;
import com.pontificia.remashorario.modules.course.CourseService;
import com.pontificia.remashorario.modules.learningSpace.LearningSpaceEntity;
import com.pontificia.remashorario.modules.learningSpace.LearningSpaceService;
import com.pontificia.remashorario.modules.learningSpace.mapper.LearningSpaceMapper;
import com.pontificia.remashorario.modules.studentGroup.StudentGroupEntity;
import com.pontificia.remashorario.modules.studentGroup.StudentGroupService;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teacher.TeacherService;
import com.pontificia.remashorario.modules.teacher.mapper.TeacherMapper;
import com.pontificia.remashorario.modules.teacherAvailability.TeacherAvailabilityEntity;
import com.pontificia.remashorario.modules.teacherAvailability.TeacherAvailabilityRepository;
import com.pontificia.remashorario.modules.teacherAvailability.TeacherAvailabilityService;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourRepository;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourService;
import com.pontificia.remashorario.modules.teachingHour.mapper.TeachingHourMapper;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeService;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
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
    private final TeacherAvailabilityRepository teacherAvailabilityRepository;
    private final TeachingHourRepository teachingHourRepository;
    private final TeachingHourService teachingHourService;

    // Mappers necesarios
    private final TeacherMapper teacherMapper;
    private final LearningSpaceMapper learningSpaceMapper;
    private final TeachingHourMapper teachingHourMapper;
    private final TimeSlotService timeSlotService;

    @Autowired
    public ClassSessionService(ClassSessionRepository classSessionRepository,
                               ClassSessionMapper classSessionMapper,
                               StudentGroupService studentGroupService,
                               CourseService courseService,
                               TeacherService teacherService,
                               LearningSpaceService learningSpaceService,
                               TeachingTypeService teachingTypeService,
                               TeacherAvailabilityService teacherAvailabilityService,
                               TeacherAvailabilityRepository teacherAvailabilityRepository,
                               TeachingHourRepository teachingHourRepository,
                               TeachingHourService teachingHourService,
                               TeacherMapper teacherMapper,
                               LearningSpaceMapper learningSpaceMapper,
                               TeachingHourMapper teachingHourMapper, TimeSlotService timeSlotService) {
        super(classSessionRepository);
        this.classSessionRepository = classSessionRepository;
        this.classSessionMapper = classSessionMapper;
        this.studentGroupService = studentGroupService;
        this.courseService = courseService;
        this.teacherService = teacherService;
        this.learningSpaceService = learningSpaceService;
        this.teachingTypeService = teachingTypeService;
        this.teacherAvailabilityService = teacherAvailabilityService;
        this.teacherAvailabilityRepository = teacherAvailabilityRepository;
        this.teachingHourRepository = teachingHourRepository;
        this.teachingHourService = teachingHourService;
        this.teacherMapper = teacherMapper;
        this.learningSpaceMapper = learningSpaceMapper;
        this.teachingHourMapper = teachingHourMapper;
        this.timeSlotService = timeSlotService;
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

    public IntelliSenseDTO getIntelliSense(UUID courseUuid, UUID groupUuid, String dayOfWeek, UUID timeSlotUuid) {
        IntelliSenseDTO intelliSense = IntelliSenseDTO.builder().build();
        List<String> recommendations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Si se proporciona un curso, filtrar docentes por área de conocimiento
        if (courseUuid != null) {
            CourseEntity course = courseService.findCourseOrThrow(courseUuid);
            List<TeacherEntity> eligibleTeachers = teacherService.getTeachersByKnowledgeArea(
                    course.getTeachingKnowledgeArea().getUuid()); // CORREGIDO: era getKnowledgeArea()
            intelliSense.setEligibleTeachers(teacherMapper.toResponseDTOList(eligibleTeachers));

            // Filtrar aulas por tipo de sesión requerido
            List<LearningSpaceEntity> eligibleSpaces = learningSpaceService.getSpacesByTeachingType(
                    course.getWeeklyPracticeHours() > 0 ? "PRACTICE" : "THEORY");
            intelliSense.setEligibleSpaces(learningSpaceMapper.toResponseDTOList(eligibleSpaces));

            // Agregar recomendaciones específicas del curso
            if (course.getWeeklyPracticeHours() > 0) {
                recommendations.add("Este curso requiere laboratorio para clases prácticas");
            }
            if (course.getPreferredSpecialty() != null) {
                recommendations.add("Recomendado: Laboratorio de " + course.getPreferredSpecialty().getName());
            }
        }

        // Si se proporciona día y turno, filtrar horas disponibles
        if (dayOfWeek != null && timeSlotUuid != null) {
            List<TeachingHourEntity> availableHours = timeSlotService.getAvailableHoursByTimeSlot(
                    timeSlotUuid, dayOfWeek);
            intelliSense.setAvailableHours(teachingHourMapper.toResponseDTOList(availableHours));
        }

        intelliSense.setRecommendations(recommendations);
        intelliSense.setWarnings(warnings);

        return intelliSense;
    }

    public ValidationResultDTO validateAssignmentInRealTime(ClassSessionValidationDTO dto) {
        return validateAssignmentInRealTime(dto, null); // Para creación
    }


    public ValidationResultDTO validateAssignmentInRealTime(ClassSessionValidationDTO dto, UUID excludeSessionUuid) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        String conflictType = null;
        String severity = "LOW";

        try {
            // Obtener entidades
            CourseEntity course = courseService.findCourseOrThrow(dto.getCourseUuid());
            TeacherEntity teacher = teacherService.findTeacherOrThrow(dto.getTeacherUuid());
            LearningSpaceEntity space = learningSpaceService.findOrThrow(dto.getLearningSpaceUuid());
            StudentGroupEntity group = studentGroupService.findOrThrow(dto.getStudentGroupUuid());
            Set<TeachingHourEntity> hours = getAndValidateTeachingHours(dto.getTeachingHourUuids());

            // ✅ OBTENER EL TIPO DE SESIÓN ACTUAL (no del curso)
            TeachingTypeEntity sessionType = teachingTypeService.findTeachingTypeOrThrow(dto.getSessionTypeUuid());

            System.out.println("=== VALIDATION DEBUG ===");
            System.out.println("Session Type UUID: " + dto.getSessionTypeUuid());
            System.out.println("Session Type: " + sessionType.getName());
            System.out.println("Learning Space Type: " + space.getTypeUUID().getName());
            System.out.println("Course supported types: " + course.getTeachingTypes().stream()
                    .map(tt -> tt.getName().name()).collect(Collectors.toList()));

            // Validar compatibilidad docente-curso
            if (!teacher.getKnowledgeAreas().contains(course.getTeachingKnowledgeArea())) {
                warnings.add("El docente no tiene el área de conocimiento específica del curso");
                suggestions.add("Considerar asignar un docente especializado en " + course.getTeachingKnowledgeArea().getName());
            }

            // Validar disponibilidad del docente
            boolean teacherAvailable = validateTeacherAvailabilityForDay(teacher, dto.getDayOfWeek(), hours);
            if (!teacherAvailable) {
                errors.add("El docente no está disponible en este horario");
                conflictType = "TEACHER";
                severity = "HIGH";
            }

            // Validar capacidad del aula
            if (space.getCapacity() < 25) {
                warnings.add("El aula podría ser pequeña para el grupo");
                suggestions.add("Considerar un aula con mayor capacidad");
            }

            // ✅ CORRECCIÓN PRINCIPAL: Validar tipo de aula vs tipo de SESIÓN (no curso)
            if (!sessionType.getName().equals(space.getTypeUUID().getName())) {
                // Solo generar warning/error si hay incompatibilidad real
                String sessionTypeName = sessionType.getName().name();
                String spaceTypeName = space.getTypeUUID().getName().name();

                System.out.println("Session type: " + sessionTypeName + ", Space type: " + spaceTypeName);

                if (sessionTypeName.equals("PRACTICE") && spaceTypeName.equals("THEORY")) {
                    warnings.add("Sesión práctica asignada a aula teórica");
                    suggestions.add("Recomendado: Usar un laboratorio para mejor experiencia de aprendizaje");
                    if (severity.equals("LOW")) severity = "MEDIUM";
                } else if (sessionTypeName.equals("THEORY") && spaceTypeName.equals("PRACTICE")) {
                    // Esto es menos crítico - una clase teórica en laboratorio está bien
                    suggestions.add("Clase teórica en laboratorio - está bien, pero un aula tradicional podría ser más apropiada");
                }
            }

            // ✅ VERIFICAR QUE EL CURSO SOPORTE EL TIPO DE SESIÓN SELECCIONADO
            boolean courseSupportsSessionType = course.getTeachingTypes().stream()
                    .anyMatch(type -> type.getName().equals(sessionType.getName()));

            if (!courseSupportsSessionType) {
                errors.add("El curso no soporta el tipo de sesión seleccionado");
                severity = "CRITICAL";
            }

            // Verificar conflictos excluyendo la sesión actual
            List<ClassSessionEntity> conflicts = findConflictsForAssignment(
                    teacher.getUuid(), space.getUuid(), group.getUuid(),
                    dto.getDayOfWeek(), hours, excludeSessionUuid);

            if (!conflicts.isEmpty()) {
                errors.add("Existe conflicto de horario");
                conflictType = determineConflictType(conflicts, teacher.getUuid(), space.getUuid(), group.getUuid());
                severity = "CRITICAL";

                conflicts.forEach(conflict -> {
                    suggestions.add("Conflicto con: " + conflict.getCourse().getName() +
                            " - " + conflict.getTeacher().getFullName());
                });
            }

            // Validar horas consecutivas
            if (hours.size() > 1 && !areHoursConsecutive(hours)) {
                warnings.add("Las horas pedagógicas no son consecutivas");
                suggestions.add("Recomendado: Asignar horas consecutivas para mejor continuidad");
            }

            // Validar duración de la sesión
            int totalMinutes = hours.stream().mapToInt(TeachingHourEntity::getDurationMinutes).sum();
            if (totalMinutes > 180) { // Más de 3 horas
                warnings.add("Sesión muy larga (más de 3 horas)");
                suggestions.add("Considerar dividir en sesiones más cortas");
                if (severity.equals("LOW")) severity = "MEDIUM";
            }

        } catch (Exception e) {
            errors.add("Error en la validación: " + e.getMessage());
            severity = "CRITICAL";
            e.printStackTrace();
        }

        return ValidationResultDTO.builder()
                .isValid(errors.isEmpty())
                .errors(errors)
                .warnings(warnings)
                .suggestions(suggestions)
                .conflictType(conflictType)
                .severity(severity)
                .build();
    }
    // 2. ✅ NUEVO MÉTODO: findConflictsForAssignment con exclusión
    private List<ClassSessionEntity> findConflictsForAssignment(
            UUID teacherUuid, UUID spaceUuid, UUID groupUuid, String dayOfWeek,
            Set<TeachingHourEntity> hours, UUID excludeSessionUuid) {

        List<ClassSessionEntity> allConflicts = new ArrayList<>();

        for (TeachingHourEntity hour : hours) {
            // Buscar conflictos separadamente para cada recurso

            // ✅ Conflictos de DOCENTE
            List<ClassSessionEntity> teacherConflicts = classSessionRepository.findConflicts(
                    teacherUuid, null, null, // Solo pasar teacherUuid
                    dayOfWeek.toUpperCase(),
                    hour.getStartTime().toString(),
                    hour.getEndTime().toString());

            // ✅ Conflictos de AULA
            List<ClassSessionEntity> spaceConflicts = classSessionRepository.findConflicts(
                    null, spaceUuid, null, // Solo pasar spaceUuid
                    dayOfWeek.toUpperCase(),
                    hour.getStartTime().toString(),
                    hour.getEndTime().toString());

            // ✅ Conflictos de GRUPO
            List<ClassSessionEntity> groupConflicts = classSessionRepository.findConflicts(
                    null, null, groupUuid, // Solo pasar groupUuid
                    dayOfWeek.toUpperCase(),
                    hour.getStartTime().toString(),
                    hour.getEndTime().toString());

            allConflicts.addAll(teacherConflicts);
            allConflicts.addAll(spaceConflicts);
            allConflicts.addAll(groupConflicts);
        }

        // ✅ Eliminar duplicados y excluir la sesión actual si es edición
        return allConflicts.stream()
                .distinct()
                .filter(session -> excludeSessionUuid == null || !session.getUuid().equals(excludeSessionUuid))
                .collect(Collectors.toList());
    }

    // 3. ✅ ACTUALIZAR el método checkConflicts para modo edición
    public ValidationResultDTO checkConflicts(ClassSessionRequestDTO dto) {
        return checkConflicts(dto, null);
    }

    public ValidationResultDTO checkConflicts(ClassSessionRequestDTO dto, UUID excludeSessionUuid) {
        return validateAssignmentInRealTime(ClassSessionValidationDTO.builder()
                .courseUuid(dto.getCourseUuid())
                .teacherUuid(dto.getTeacherUuid())
                .learningSpaceUuid(dto.getLearningSpaceUuid())
                .studentGroupUuid(dto.getStudentGroupUuid())
                .dayOfWeek(dto.getDayOfWeek().name())
                .teachingHourUuids(dto.getTeachingHourUuids())
                .build(), excludeSessionUuid);
    }

    private String determineConflictType(List<ClassSessionEntity> conflicts, UUID teacherUuid, UUID spaceUuid, UUID groupUuid) {
        boolean hasTeacherConflict = conflicts.stream().anyMatch(c -> c.getTeacher().getUuid().equals(teacherUuid));
        boolean hasSpaceConflict = conflicts.stream().anyMatch(c -> c.getLearningSpace().getUuid().equals(spaceUuid));
        boolean hasGroupConflict = conflicts.stream().anyMatch(c -> c.getStudentGroup().getUuid().equals(groupUuid));

        int conflictCount = (hasTeacherConflict ? 1 : 0) + (hasSpaceConflict ? 1 : 0) + (hasGroupConflict ? 1 : 0);

        if (conflictCount > 1) return "MULTIPLE";
        if (hasTeacherConflict) return "TEACHER";
        if (hasSpaceConflict) return "SPACE";
        if (hasGroupConflict) return "GROUP";
        return "UNKNOWN";
    }

    public List<ClassSessionResponseDTO> getSessionsByStudentGroupAndPeriod(UUID groupUuid, UUID periodUuid) {
        List<ClassSessionEntity> sessions = classSessionRepository
                .findByStudentGroupUuidAndPeriod(groupUuid, periodUuid);
        return classSessionMapper.toResponseDTOList(sessions);
    }

    public List<ClassSessionResponseDTO> getSessionsByTeacherAndPeriod(UUID teacherUuid, UUID periodUuid) {
        List<ClassSessionEntity> sessions = classSessionRepository.findByTeacherUuidAndPeriod(teacherUuid, periodUuid);
        return classSessionMapper.toResponseDTOList(sessions);
    }

    public List<ClassSessionResponseDTO> getSessionsByPeriod(UUID periodUuid) {
        List<ClassSessionEntity> sessions = classSessionRepository.findByPeriod(periodUuid);
        return classSessionMapper.toResponseDTOList(sessions);
    }

    private List<ClassSessionEntity> findConflictsForAssignment(
            UUID teacherUuid, UUID spaceUuid, UUID groupUuid, String dayOfWeek, Set<TeachingHourEntity> hours) {

        List<ClassSessionEntity> allConflicts = new ArrayList<>();

        for (TeachingHourEntity hour : hours) {
            List<ClassSessionEntity> conflicts = classSessionRepository.findConflicts(
                    teacherUuid, spaceUuid, groupUuid,
                    dayOfWeek.toUpperCase(),
                    hour.getStartTime().toString(),
                    hour.getEndTime().toString());
            allConflicts.addAll(conflicts);
        }

        return allConflicts.stream().distinct().collect(Collectors.toList());
    }

    private boolean areHoursConsecutive(Set<TeachingHourEntity> hours) {
        if (hours.size() <= 1) return true;

        List<TeachingHourEntity> sortedHours = hours.stream()
                .sorted(Comparator.comparing(TeachingHourEntity::getOrderInTimeSlot))
                .collect(Collectors.toList());

        for (int i = 1; i < sortedHours.size(); i++) {
            if (sortedHours.get(i).getOrderInTimeSlot() != sortedHours.get(i-1).getOrderInTimeSlot() + 1) {
                return false;
            }
        }
        return true;
    }

    private boolean validateTeacherAvailabilityForDay(TeacherEntity teacher, String dayOfWeek, Set<TeachingHourEntity> hours) {
        // Obtener disponibilidades del docente para el día específico
        List<TeacherAvailabilityEntity> availabilities = teacherAvailabilityRepository
                .findByTeacherAndDayOfWeek(teacher, DayOfWeek.valueOf(dayOfWeek.toUpperCase()));

        if (availabilities.isEmpty()) return false; // No hay disponibilidad registrada

        // Verificar si todas las horas están dentro de la disponibilidad del docente
        for (TeachingHourEntity hour : hours) {
            boolean isAvailable = availabilities.stream().anyMatch(availability ->
                    availability.getIsAvailable() &&
                            hour.getStartTime().compareTo(availability.getStartTime()) >= 0 &&
                            hour.getEndTime().compareTo(availability.getEndTime()) <= 0
            );

            if (!isAvailable) return false;
        }

        return true;
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
        validateNoConflicts(dto, studentGroup.getPeriod().getUuid(), teachingHours);

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
        validateNoConflicts(dto, studentGroup.getPeriod().getUuid(), teachingHours, uuid); // Excluir la sesión actual

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

    private void validateNoConflicts(ClassSessionRequestDTO dto, UUID periodUuid, Set<TeachingHourEntity> teachingHours) {
        validateNoConflicts(dto, periodUuid, teachingHours, null);
    }

    private void validateNoConflicts(ClassSessionRequestDTO dto, UUID periodUuid, Set<TeachingHourEntity> teachingHours, UUID excludeSessionUuid) {
        List<UUID> teachingHourUuids = teachingHours.stream()
                .map(TeachingHourEntity::getUuid)
                .collect(Collectors.toList());

        // Verificar conflictos de docente
        List<ClassSessionEntity> teacherConflicts = classSessionRepository.findTeacherConflicts(
                dto.getTeacherUuid(), dto.getDayOfWeek(), periodUuid, teachingHourUuids);
        if (excludeSessionUuid != null) {
            teacherConflicts.removeIf(session -> session.getUuid().equals(excludeSessionUuid));
        }
        if (!teacherConflicts.isEmpty()) {
            throw new IllegalArgumentException("El docente ya tiene una clase asignada en ese horario");
        }

        // Verificar conflictos de aula
        List<ClassSessionEntity> spaceConflicts = classSessionRepository.findLearningSpaceConflicts(
                dto.getLearningSpaceUuid(), dto.getDayOfWeek(), periodUuid, teachingHourUuids);
        if (excludeSessionUuid != null) {
            spaceConflicts.removeIf(session -> session.getUuid().equals(excludeSessionUuid));
        }
        if (!spaceConflicts.isEmpty()) {
            throw new IllegalArgumentException("El aula ya está ocupada en ese horario");
        }

        // Verificar conflictos de grupo
        List<ClassSessionEntity> groupConflicts = classSessionRepository.findStudentGroupConflicts(
                dto.getStudentGroupUuid(), dto.getDayOfWeek(), periodUuid, teachingHourUuids);
        if (excludeSessionUuid != null) {
            groupConflicts.removeIf(session -> session.getUuid().equals(excludeSessionUuid));
        }
        if (!groupConflicts.isEmpty()) {
            throw new IllegalArgumentException("El grupo ya tiene una clase asignada en ese horario");
        }
    }
}