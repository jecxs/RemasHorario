package com.pontificia.remashorario.modules.automaticSchedule.service;

import com.pontificia.remashorario.modules.TimeSlot.TimeSlotEntity;
import com.pontificia.remashorario.modules.TimeSlot.TimeSlotService;
import com.pontificia.remashorario.modules.TimeSlot.dto.TimeSlotResponseDTO;
import com.pontificia.remashorario.modules.automaticSchedule.context.ScheduleGenerationContext;
import com.pontificia.remashorario.modules.automaticSchedule.dto.*;
import com.pontificia.remashorario.modules.classSession.ClassSessionEntity;
import com.pontificia.remashorario.modules.classSession.ClassSessionRepository;
import com.pontificia.remashorario.modules.classSession.ClassSessionService;
import com.pontificia.remashorario.modules.classSession.dto.ClassSessionRequestDTO;
import com.pontificia.remashorario.modules.classSession.dto.ClassSessionResponseDTO;
import com.pontificia.remashorario.modules.classSession.mapper.ClassSessionMapper;
import com.pontificia.remashorario.modules.course.CourseEntity;
import com.pontificia.remashorario.modules.course.CourseService;
import com.pontificia.remashorario.modules.cycle.CycleEntity;
import com.pontificia.remashorario.modules.cycle.CycleService;
import com.pontificia.remashorario.modules.learningSpace.LearningSpaceEntity;
import com.pontificia.remashorario.modules.learningSpace.LearningSpaceService;
import com.pontificia.remashorario.modules.studentGroup.StudentGroupEntity;
import com.pontificia.remashorario.modules.studentGroup.StudentGroupService;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teacher.TeacherService;
import com.pontificia.remashorario.modules.teacher.dto.TeacherResponseDTO;
import com.pontificia.remashorario.modules.teacherAvailability.TeacherAvailabilityEntity;
import com.pontificia.remashorario.modules.teacherAvailability.TeacherAvailabilityRepository;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourRepository;
import com.pontificia.remashorario.modules.teachingHour.dto.TeachingHourResponseDTO;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeService;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AutomaticScheduleGenerationService {

    // Servicios existentes
    private final StudentGroupService studentGroupService;
    private final CourseService courseService;
    private final TeacherService teacherService;
    private final LearningSpaceService learningSpaceService;
    private final ClassSessionService classSessionService;
    private final TimeSlotService timeSlotService;
    private final TeachingHourRepository teachingHourRepository;
    private final TeacherAvailabilityRepository teacherAvailabilityRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ScheduleUtilityService scheduleUtilityService;

    // Mappers
    private final ClassSessionMapper classSessionMapper;
    private final TeachingTypeService teachingTypeService;

    /**
     * Método principal para generar horarios automáticamente
     */
    @Transactional
    public ScheduleGenerationResultDTO generateSchedule(ScheduleGenerationRequestDTO request) {
        log.info("🚀 Iniciando generación automática de horarios para periodo: {}", request.getPeriodUuid());
        long startTime = System.currentTimeMillis();

        try {
            // 1. Validar datos de entrada
            validateRequest(request);

            // 2. Obtener grupos objetivo
            List<StudentGroupEntity> targetGroups = getTargetGroups(request);
            log.info("📋 Grupos objetivo identificados: {}", targetGroups.size());

            // 3. Analizar requerimientos de cada grupo
            List<GroupRequirementDTO> groupRequirements = analyzeGroupRequirements(targetGroups);
            log.info("📊 Analizados requerimientos para {} grupos", groupRequirements.size());

            // 4. Generar horarios
            ScheduleGenerationContext context = new ScheduleGenerationContext(request, groupRequirements);
            List<GeneratedClassSessionDTO> generatedSessions = generateScheduleForGroups(context);

            // 5. Crear estadísticas y resultado
            long executionTime = System.currentTimeMillis() - startTime;
            ScheduleGenerationResultDTO result = buildResult(context, generatedSessions, executionTime);

            log.info("✅ Generación completada en {} ms. Sesiones generadas: {}",
                    executionTime, generatedSessions.size());

            return result;

        } catch (Exception e) {
            log.error("❌ Error en generación automática: {}", e.getMessage(), e);
            return ScheduleGenerationResultDTO.builder()
                    .success(false)
                    .message("Error en la generación: " + e.getMessage())
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * Genera vista previa sin crear sesiones reales
     */
    public SchedulePreviewDTO getSchedulePreview(SchedulePreviewRequestDTO request) {
        List<StudentGroupEntity> targetGroups = getTargetGroups(
                ScheduleGenerationRequestDTO.builder()
                        .periodUuid(request.getPeriodUuid())
                        .modalityUuid(request.getModalityUuid())
                        .careerUuid(request.getCareerUuid())
                        .cycleUuid(request.getCycleUuid())
                        .groupUuids(request.getGroupUuids())
                        .build()
        );

        List<GroupRequirementDTO> requirements = analyzeGroupRequirements(targetGroups);
        ScheduleConstraintsDTO constraints = analyzeConstraints(requirements);
        ScheduleFeasibilityDTO feasibility = analyzeFeasibility(requirements, constraints);

        return SchedulePreviewDTO.builder()
                .groupRequirements(requirements)
                .constraints(constraints)
                .feasibility(feasibility)
                .build();
    }

    // ===================== MÉTODOS PRINCIPALES DE GENERACIÓN =====================

    private List<GeneratedClassSessionDTO> generateScheduleForGroups(ScheduleGenerationContext context) {
        List<GeneratedClassSessionDTO> allGeneratedSessions = new ArrayList<>();

        // Mapa para tracking de asignaciones de docentes por curso
        Map<String, UUID> courseTeacherAssignments = new HashMap<>(); // "groupUuid_courseUuid" -> teacherUuid

        for (GroupRequirementDTO group : context.getGroupRequirements()) {
            log.info("🎯 Generando horario para grupo: {}", group.getGroupName());

            List<GeneratedClassSessionDTO> groupSessions = generateScheduleForGroup(
                    group, context, courseTeacherAssignments);

            allGeneratedSessions.addAll(groupSessions);

            // Actualizar contexto con las nuevas asignaciones
            updateContextWithGeneratedSessions(context, groupSessions);
        }

        return allGeneratedSessions;
    }

    private List<GeneratedClassSessionDTO> generateScheduleForGroup(
            GroupRequirementDTO group,
            ScheduleGenerationContext context,
            Map<String, UUID> courseTeacherAssignments) {

        List<GeneratedClassSessionDTO> groupSessions = new ArrayList<>();

        // Obtener slots de tiempo disponibles para la semana
        List<ScheduleSlotDTO> availableSlots = getAvailableTimeSlots(context);

        // Ordenar cursos por prioridad (más horas primero, luego por complejidad)
        List<CourseRequirementDTO> sortedCourses = prioritizeCourses(group.getCourses());

        for (CourseRequirementDTO course : sortedCourses) {
            log.info("📚 Procesando curso: {} ({}h teoría, {}h práctica)",
                    course.getCourseName(), course.getTotalTheoryHours(), course.getTotalPracticeHours());

            // Generar sesiones para este curso
            List<GeneratedClassSessionDTO> courseSessions = generateSessionsForCourse(
                    group, course, availableSlots, context, courseTeacherAssignments);

            groupSessions.addAll(courseSessions);

            // Actualizar slots disponibles
            updateAvailableSlots(availableSlots, courseSessions);
        }

        return groupSessions;
    }

    private List<GeneratedClassSessionDTO> generateSessionsForCourse(
            GroupRequirementDTO group,
            CourseRequirementDTO course,
            List<ScheduleSlotDTO> availableSlots,
            ScheduleGenerationContext context,
            Map<String, UUID> courseTeacherAssignments) {

        List<GeneratedClassSessionDTO> courseSessions = new ArrayList<>();
        String courseTeacherKey = group.getGroupUuid() + "_" + course.getCourseUuid();

        // Verificar si ya hay un docente asignado para este curso en este grupo
        UUID assignedTeacher = courseTeacherAssignments.get(courseTeacherKey);

        // Generar sesiones de teoría
        if (course.getTotalTheoryHours() > 0) {
            List<GeneratedClassSessionDTO> theorySessions = generateSessionsForType(
                    group, course, "THEORY", course.getTotalTheoryHours(),
                    availableSlots, context, assignedTeacher);

            courseSessions.addAll(theorySessions);

            // Guardar el docente asignado si es la primera asignación
            if (assignedTeacher == null && !theorySessions.isEmpty()) {
                UUID teacherFromSession = getTeacherUuidFromSessionName(theorySessions.get(0).getTeacherName());
                if (teacherFromSession != null) {
                    courseTeacherAssignments.put(courseTeacherKey, teacherFromSession);
                    assignedTeacher = teacherFromSession;
                }
            }
        }

        // Generar sesiones de práctica
        if (course.getTotalPracticeHours() > 0) {
            List<GeneratedClassSessionDTO> practiceSessions = generateSessionsForType(
                    group, course, "PRACTICE", course.getTotalPracticeHours(),
                    availableSlots, context, assignedTeacher);

            courseSessions.addAll(practiceSessions);
        }

        return courseSessions;
    }

    /**
     * Método actualizado para generar sesiones con mejor manejo de errores
     * ACTUALIZAR el método generateSessionsForType existente
     */
    private List<GeneratedClassSessionDTO> generateSessionsForType(
            GroupRequirementDTO group,
            CourseRequirementDTO course,
            String sessionType,
            Integer totalHours,
            List<ScheduleSlotDTO> availableSlots,
            ScheduleGenerationContext context,
            UUID preferredTeacher) {

        List<GeneratedClassSessionDTO> sessions = new ArrayList<>();
        int remainingHours = totalHours;
        int maxAttempts = 50; // Evitar bucles infinitos
        int attempts = 0;

        // Configuración de distribución
        int maxConsecutiveHours = context.getRequest().getMaxConsecutiveHours();
        boolean distributeEvenly = context.getRequest().getDistributeEvenly();

        while (remainingHours > 0 && attempts < maxAttempts) {
            attempts++;

            // Determinar cuántas horas asignar en esta iteración
            int hoursForThisSession = Math.min(maxConsecutiveHours, remainingHours);

            // Buscar el mejor slot disponible
            AssignmentCandidateDTO bestCandidate = findBestAssignmentSlot(
                    group, course, sessionType, hoursForThisSession,
                    availableSlots, context, preferredTeacher);

            if (bestCandidate == null) {
                // Si no se puede con las horas pedidas, intentar con menos
                if (hoursForThisSession > 1) {
                    hoursForThisSession = 1;
                    bestCandidate = findBestAssignmentSlot(
                            group, course, sessionType, hoursForThisSession,
                            availableSlots, context, preferredTeacher);
                }

                if (bestCandidate == null) {
                    log.warn("⚠️ No se pudo encontrar slot para {} - {} ({} horas restantes)",
                            course.getCourseName(), sessionType, remainingHours);
                    break;
                }
            }

            // ✅ CAMBIO PRINCIPAL: Crear sesión real con mejor manejo de errores
            boolean sessionCreated = createRealClassSession(bestCandidate, context.getRequest().getPeriodUuid(), context);

            if (sessionCreated) {
                // Crear DTO de sesión generada solo si se creó exitosamente
                GeneratedClassSessionDTO sessionDTO = createSessionFromCandidate(bestCandidate, group, course, sessionType);
                sessions.add(sessionDTO);

                remainingHours -= hoursForThisSession;

                // Marcar slot como ocupado
                markSlotAsOccupied(availableSlots, bestCandidate);

                log.info("✅ Creada sesión: {} - {} - {} ({} horas)",
                        course.getCourseName(), sessionType,
                        bestCandidate.getDayOfWeek(), hoursForThisSession);
            } else {
                // Si no se pudo crear, remover este slot de consideración
                removeFailedSlotFromAvailable(availableSlots, bestCandidate);
                log.warn("⚠️ No se pudo crear sesión, removiendo slot problemático");
            }
        }

        if (remainingHours > 0) {
            String message = String.format("No se pudieron asignar todas las horas para %s %s. Faltan %d horas",
                    course.getCourseName(), sessionType, remainingHours);
            context.addWarning("Asignación incompleta", message, "MEDIUM");
            log.warn("⚠️ {}", message);
        }

        return sessions;
    }
    /**
     * Remueve un slot problemático de la lista de disponibles
     */
    private void removeFailedSlotFromAvailable(List<ScheduleSlotDTO> availableSlots, AssignmentCandidateDTO failedCandidate) {
        availableSlots.removeIf(slot ->
                slot.getDayOfWeek().equals(failedCandidate.getDayOfWeek()) &&
                        slot.getTimeSlotUuid().equals(failedCandidate.getTimeSlotUuid())
        );
    }

    // ===================== MÉTODOS DE BÚSQUEDA Y ASIGNACIÓN =====================

    private AssignmentCandidateDTO findBestAssignmentSlot(
            GroupRequirementDTO group,
            CourseRequirementDTO course,
            String sessionType,
            int requiredHours,
            List<ScheduleSlotDTO> availableSlots,
            ScheduleGenerationContext context,
            UUID preferredTeacher) {

        List<AssignmentCandidateDTO> candidates = new ArrayList<>();

        for (ScheduleSlotDTO slot : availableSlots) {
            // Saltar días excluidos
            if (context.getRequest().getExcludedDays().contains(slot.getDayOfWeek())) {
                continue;
            }

            // Verificar si hay suficientes horas consecutivas disponibles
            List<UUID> availableHours = getConsecutiveHours(slot, requiredHours);
            if (availableHours.size() < requiredHours) {
                continue;
            }

            // ✅ NUEVA VALIDACIÓN: Verificar que las horas estén realmente disponibles
            if (!areHoursAvailable(availableHours, slot, context.getRequest().getPeriodUuid())) {
                continue;
            }

            // Buscar docentes disponibles
            List<TeacherAvailabilitySlotDTO> availableTeachers =
                    getAvailableTeachers(course, slot, availableHours, preferredTeacher);

            if (availableTeachers.isEmpty()) {
                continue;
            }

            // Buscar aulas disponibles
            List<SpaceAvailabilitySlotDTO> availableSpaces =
                    getAvailableSpaces(course, sessionType, slot, availableHours);

            if (availableSpaces.isEmpty()) {
                continue;
            }

            // Crear candidatos combinando docentes y aulas
            for (TeacherAvailabilitySlotDTO teacher : availableTeachers) {
                for (SpaceAvailabilitySlotDTO space : availableSpaces) {
                    AssignmentCandidateDTO candidate = AssignmentCandidateDTO.builder()
                            .groupUuid(group.getGroupUuid())
                            .courseUuid(course.getCourseUuid())
                            .teacherUuid(teacher.getTeacherUuid())
                            .spaceUuid(space.getSpaceUuid())
                            .dayOfWeek(slot.getDayOfWeek())
                            .timeSlotUuid(slot.getTimeSlotUuid())
                            .teachingHourUuids(availableHours) // ✅ Ahora son garantizadamente consecutivas
                            .sessionType(sessionType)
                            .build();

                    // Calcular puntuación del candidato
                    double score = calculateAssignmentScore(candidate, slot, teacher, space, context);
                    candidate.setScore(score);

                    candidates.add(candidate);
                }
            }
        }

        // Ordenar por puntuación y devolver el mejor
        return candidates.stream()
                .max(Comparator.comparing(AssignmentCandidateDTO::getScore))
                .orElse(null);
    }

    private double calculateAssignmentScore(
            AssignmentCandidateDTO candidate,
            ScheduleSlotDTO slot,
            TeacherAvailabilitySlotDTO teacher,
            SpaceAvailabilitySlotDTO space,
            ScheduleGenerationContext context) {

        double score = 100.0; // Puntuación base

        // Factor 1: Turno preferente (+20 puntos)
        if (slot.getIsPreferred()) {
            score += 20.0;
        }

        // Factor 2: Aula con especialidad preferida (+15 puntos)
        if (space.getIsPreferred()) {
            score += 15.0;
        }

        // Factor 3: Docente preferido/asignado previamente (+25 puntos)
        if (teacher.getTeacherUuid().equals(candidate.getTeacherUuid())) {
            score += 25.0;
        }

        // Factor 4: Evitar huecos de tiempo (+10 puntos por continuidad)
        if (context.getRequest().getAvoidTimeGaps()) {
            boolean hasContinuity = checkTimeContinuity(candidate, context);
            if (hasContinuity) {
                score += 10.0;
            }
        }

        // Factor 5: Distribución equilibrada (+5 a +15 puntos)
        if (context.getRequest().getDistributeEvenly()) {
            double distributionBonus = calculateDistributionBonus(candidate, context);
            score += distributionBonus;
        }

        // Factor 6: Penalización por día muy cargado (-5 a -20 puntos)
        int dailyHours = getDailyHoursForGroup(candidate.getGroupUuid(), candidate.getDayOfWeek(), context);
        if (dailyHours > context.getRequest().getMaxHoursPerDay()) {
            score -= (dailyHours - context.getRequest().getMaxHoursPerDay()) * 5.0;
        }

        // Factor 7: Bonus por capacidad del aula adecuada (+5 puntos)
        if (space.getCapacity() >= 25 && space.getCapacity() <= 40) {
            score += 5.0;
        }

        return Math.max(0, score); // No permitir puntuaciones negativas
    }

    // ===================== MÉTODOS DE ANÁLISIS Y VALIDACIÓN =====================

    private List<GroupRequirementDTO> analyzeGroupRequirements(List<StudentGroupEntity> groups) {
        List<GroupRequirementDTO> requirements = new ArrayList<>();

        for (StudentGroupEntity group : groups) {
            List<CourseEntity> groupCourses = courseService.getCoursesByCycle(group.getCycle().getUuid());
            List<CourseRequirementDTO> courseRequirements = new ArrayList<>();
            int totalWeeklyHours = 0;

            for (CourseEntity course : groupCourses) {
                CourseRequirementDTO courseReq = CourseRequirementDTO.builder()
                        .courseUuid(course.getUuid())
                        .courseName(course.getName())
                        .cycleUuid(course.getCycle().getUuid())
                        .totalTheoryHours(course.getWeeklyTheoryHours())
                        .totalPracticeHours(course.getWeeklyPracticeHours())
                        .totalHours(course.getWeeklyTheoryHours() + course.getWeeklyPracticeHours())
                        .requiredKnowledgeAreaUuid(course.getTeachingKnowledgeArea().getUuid())
                        .preferredSpecialtyUuid(course.getPreferredSpecialty() != null ?
                                course.getPreferredSpecialty().getUuid() : null)
                        .supportedTeachingTypeUuids(course.getTeachingTypes().stream()
                                .map(BaseEntity::getUuid)
                                .collect(Collectors.toList()))
                        .isMixed(course.getWeeklyTheoryHours() > 0 && course.getWeeklyPracticeHours() > 0)
                        .build();

                courseRequirements.add(courseReq);
                totalWeeklyHours += courseReq.getTotalHours();
            }

            GroupRequirementDTO groupReq = GroupRequirementDTO.builder()
                    .groupUuid(group.getUuid())
                    .groupName(group.getName())
                    .cycleUuid(group.getCycle().getUuid())
                    .periodUuid(group.getPeriod().getUuid())
                    .courses(courseRequirements)
                    .totalWeeklyHours(totalWeeklyHours)
                    .build();

            requirements.add(groupReq);
        }

        return requirements;
    }

    // ===================== MÉTODOS DE UTILIDAD Y CONTEXTO =====================

    private List<StudentGroupEntity> getTargetGroups(ScheduleGenerationRequestDTO request) {
        if (request.getGroupUuids() != null && !request.getGroupUuids().isEmpty()) {
            return request.getGroupUuids().stream()
                    .map(studentGroupService::findOrThrow)
                    .collect(Collectors.toList());
        }

        // Si no se especifican grupos, obtener por otros filtros
        List<StudentGroupEntity> allGroups = studentGroupService.getGroupsByPeriod(request.getPeriodUuid())
                .stream()
                .map(dto -> studentGroupService.findOrThrow(dto.getUuid()))
                .collect(Collectors.toList());

        // Filtrar por modalidad, carrera o ciclo si se especifica
        if (request.getCycleUuid() != null) {
            return allGroups.stream()
                    .filter(group -> group.getCycle().getUuid().equals(request.getCycleUuid()))
                    .collect(Collectors.toList());
        }

        if (request.getCareerUuid() != null) {
            return allGroups.stream()
                    .filter(group -> group.getCycle().getCareer().getUuid().equals(request.getCareerUuid()))
                    .collect(Collectors.toList());
        }

        if (request.getModalityUuid() != null) {
            return allGroups.stream()
                    .filter(group -> group.getCycle().getCareer().getModality().getUuid().equals(request.getModalityUuid()))
                    .collect(Collectors.toList());
        }

        return allGroups;
    }

    private void validateRequest(ScheduleGenerationRequestDTO request) {
        if (!request.isValidScope()) {
            throw new IllegalArgumentException("Debe especificar al menos un filtro de alcance para la generación");
        }

        if (request.getMaxHoursPerDay() < request.getMinHoursPerDay()) {
            throw new IllegalArgumentException("El máximo de horas por día no puede ser menor al mínimo");
        }

        if (request.getMaxConsecutiveHours() > request.getMaxHoursPerDay()) {
            throw new IllegalArgumentException("Las horas consecutivas no pueden exceder el máximo diario");
        }
    }

    private List<ScheduleSlotDTO> getAvailableTimeSlots(ScheduleGenerationContext context) {
        List<ScheduleSlotDTO> slots = new ArrayList<>();
        List<TimeSlotResponseDTO> timeSlots = timeSlotService.getAllTimeSlots();

        // Días de la semana (lunes a sábado por defecto)
        List<DayOfWeek> workDays = Arrays.asList(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
        );

        for (DayOfWeek day : workDays) {
            for (TimeSlotResponseDTO timeSlot : timeSlots) {
                boolean isPreferred = context.getRequest().getPreferredTimeSlotUuids().contains(timeSlot.getUuid());

                ScheduleSlotDTO slot = ScheduleSlotDTO.builder()
                        .dayOfWeek(day)
                        .timeSlotUuid(timeSlot.getUuid())
                        .timeSlotName(timeSlot.getName())
                        .teachingHourUuids(timeSlot.getTeachingHours().stream()
                                .map(TeachingHourResponseDTO::getUuid)
                                .collect(Collectors.toList()))
                        .startTime(timeSlot.getStartTime())
                        .endTime(timeSlot.getEndTime())
                        .durationMinutes((int) Duration.between(timeSlot.getStartTime(), timeSlot.getEndTime()).toMinutes())
                        .isPreferred(isPreferred)
                        .build();

                slots.add(slot);
            }
        }

        return slots;
    }

    private List<CourseRequirementDTO> prioritizeCourses(List<CourseRequirementDTO> courses) {
        return courses.stream()
                .sorted((c1, c2) -> {
                    // Prioridad 1: Cursos con más horas totales
                    int hourComparison = Integer.compare(c2.getTotalHours(), c1.getTotalHours());
                    if (hourComparison != 0) return hourComparison;

                    // Prioridad 2: Cursos mixtos (teoría + práctica)
                    boolean c1Mixed = c1.getIsMixed();
                    boolean c2Mixed = c2.getIsMixed();
                    if (c1Mixed && !c2Mixed) return -1;
                    if (!c1Mixed && c2Mixed) return 1;

                    // Prioridad 3: Cursos con especialidad preferida
                    boolean c1HasSpecialty = c1.getPreferredSpecialtyUuid() != null;
                    boolean c2HasSpecialty = c2.getPreferredSpecialtyUuid() != null;
                    if (c1HasSpecialty && !c2HasSpecialty) return -1;
                    if (!c1HasSpecialty && c2HasSpecialty) return 1;

                    // Por último, alfabético
                    return c1.getCourseName().compareTo(c2.getCourseName());
                })
                .collect(Collectors.toList());
    }

    private List<UUID> getConsecutiveHours(ScheduleSlotDTO slot, int requiredHours) {
        List<UUID> allHours = slot.getTeachingHourUuids();
        if (allHours.size() < requiredHours) {
            return new ArrayList<>();
        }

        // Obtener las entidades de TeachingHour y ordenarlas por posición
        List<TeachingHourEntity> teachingHours = allHours.stream()
                .map(uuid -> teachingHourRepository.findById(uuid).orElse(null))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(TeachingHourEntity::getOrderInTimeSlot))
                .collect(Collectors.toList());

        if (teachingHours.size() < requiredHours) {
            return new ArrayList<>();
        }

        // Buscar secuencias consecutivas de la longitud requerida
        for (int i = 0; i <= teachingHours.size() - requiredHours; i++) {
            List<TeachingHourEntity> candidate = teachingHours.subList(i, i + requiredHours);

            if (areConsecutive(candidate)) {
                return candidate.stream()
                        .map(TeachingHourEntity::getUuid)
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>(); // No se encontraron horas consecutivas
    }
    /**
     * Verifica si una lista de horas pedagógicas son consecutivas
     */
    private boolean areConsecutive(List<TeachingHourEntity> hours) {
        if (hours.size() <= 1) {
            return true;
        }

        for (int i = 0; i < hours.size() - 1; i++) {
            TeachingHourEntity current = hours.get(i);
            TeachingHourEntity next = hours.get(i + 1);

            // Verificar que pertenezcan al mismo turno
            if (!current.getTimeSlot().getUuid().equals(next.getTimeSlot().getUuid())) {
                return false;
            }

            // Verificar que sean consecutivas en orden
            if (current.getOrderInTimeSlot() + 1 != next.getOrderInTimeSlot()) {
                return false;
            }

            // Verificar que no haya gaps de tiempo
            if (!current.getEndTime().equals(next.getStartTime())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Método auxiliar para verificar disponibilidad de horas específicas
     * AGREGAR este método también
     */
    private boolean areHoursAvailable(List<UUID> teachingHourUuids, ScheduleSlotDTO slot, UUID periodUuid) {
        // Verificar que no haya conflictos existentes para estas horas específicas
        List<ClassSessionEntity> conflicts = classSessionRepository.findAll().stream()
                .filter(session -> session.getPeriod().getUuid().equals(periodUuid))
                .filter(session -> session.getDayOfWeek().equals(slot.getDayOfWeek()))
                .filter(session -> session.getTeachingHours().stream()
                        .anyMatch(hour -> teachingHourUuids.contains(hour.getUuid())))
                .collect(Collectors.toList());

        return conflicts.isEmpty();
    }


    private List<TeacherAvailabilitySlotDTO> getAvailableTeachers(
            CourseRequirementDTO course,
            ScheduleSlotDTO slot,
            List<UUID> teachingHourUuids,
            UUID preferredTeacher) {

        List<TeacherAvailabilitySlotDTO> availableTeachers = new ArrayList<>();

        // Obtener docentes con el área de conocimiento requerida
        List<TeacherEntity> eligibleTeachers = teacherService.getTeachersByKnowledgeArea(
                course.getRequiredKnowledgeAreaUuid());

        for (TeacherEntity teacher : eligibleTeachers) {
            // Verificar disponibilidad en el horario específico
            List<TeacherAvailabilityEntity> availabilities = teacherAvailabilityRepository
                    .findByTeacherAndDayOfWeek(teacher, slot.getDayOfWeek());

            boolean isAvailable = isTeacherAvailableForHours(teacher, slot, teachingHourUuids, availabilities);

            if (isAvailable) {
                TeacherAvailabilitySlotDTO teacherSlot = TeacherAvailabilitySlotDTO.builder()
                        .teacherUuid(teacher.getUuid())
                        .teacherName(teacher.getFullName())
                        .dayOfWeek(slot.getDayOfWeek())
                        .timeSlotUuid(slot.getTimeSlotUuid())
                        .timeSlotName(slot.getTimeSlotName())
                        .availableTeachingHourUuids(teachingHourUuids)
                        .knowledgeAreaUuids(teacher.getKnowledgeAreas().stream()
                                .map(BaseEntity::getUuid)
                                .collect(Collectors.toList()))
                        .isPreferred(teacher.getUuid().equals(preferredTeacher))
                        .build();

                availableTeachers.add(teacherSlot);
            }
        }

        // Priorizar docente preferido si está disponible
        if (preferredTeacher != null) {
            availableTeachers.sort((t1, t2) -> {
                if (t1.getTeacherUuid().equals(preferredTeacher)) return -1;
                if (t2.getTeacherUuid().equals(preferredTeacher)) return 1;
                return 0;
            });
        }

        return availableTeachers;
    }

    private boolean isTeacherAvailableForHours(
            TeacherEntity teacher,
            ScheduleSlotDTO slot,
            List<UUID> teachingHourUuids,
            List<TeacherAvailabilityEntity> availabilities) {

        if (availabilities.isEmpty()) {
            return false; // Sin disponibilidad configurada
        }

        // Obtener las horas pedagógicas específicas
        List<TeachingHourEntity> teachingHours = teachingHourUuids.stream()
                .map(uuid -> teachingHourRepository.findById(uuid).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (teachingHours.isEmpty()) {
            return false;
        }

        // Verificar que todas las horas estén dentro de alguna disponibilidad del docente
        LocalTime sessionStart = teachingHours.stream()
                .map(TeachingHourEntity::getStartTime)
                .min(LocalTime::compareTo)
                .orElse(LocalTime.MAX);

        LocalTime sessionEnd = teachingHours.stream()
                .map(TeachingHourEntity::getEndTime)
                .max(LocalTime::compareTo)
                .orElse(LocalTime.MIN);

        return availabilities.stream().anyMatch(availability ->
                availability.getIsAvailable() &&
                        availability.getStartTime().compareTo(sessionStart) <= 0 &&
                        availability.getEndTime().compareTo(sessionEnd) >= 0
        );
    }

    private List<SpaceAvailabilitySlotDTO> getAvailableSpaces(
            CourseRequirementDTO course,
            String sessionType,
            ScheduleSlotDTO slot,
            List<UUID> teachingHourUuids) {

        List<SpaceAvailabilitySlotDTO> availableSpaces = new ArrayList<>();

        // Determinar tipo de aula requerido
        String requiredSpaceType = sessionType.equals("PRACTICE") ? "PRACTICE" : "THEORY";

        // Obtener aulas del tipo requerido
        List<LearningSpaceEntity> eligibleSpaces = learningSpaceService
                .getSpacesByTeachingType(requiredSpaceType);

        // Filtrar por especialidad si se especifica
        if ("PRACTICE".equals(requiredSpaceType) && course.getPreferredSpecialtyUuid() != null) {
            List<LearningSpaceEntity> specialtySpaces = eligibleSpaces.stream()
                    .filter(space -> space.getSpecialty() != null &&
                            space.getSpecialty().getUuid().equals(course.getPreferredSpecialtyUuid()))
                    .collect(Collectors.toList());

            if (!specialtySpaces.isEmpty()) {
                eligibleSpaces = specialtySpaces;
            }
        }

        for (LearningSpaceEntity space : eligibleSpaces) {
            // Verificar disponibilidad en el horario específico
            boolean isAvailable = learningSpaceService.isSpaceAvailableForSpecificHours(
                    space.getUuid(),
                    slot.getDayOfWeek().name(),
                    teachingHourUuids.stream().map(UUID::toString).collect(Collectors.toList())
            );

            if (isAvailable) {
                SpaceAvailabilitySlotDTO spaceSlot = SpaceAvailabilitySlotDTO.builder()
                        .spaceUuid(space.getUuid())
                        .spaceName(space.getName())
                        .spaceType(space.getTypeUUID().getName().name())
                        .specialtyUuid(space.getSpecialty() != null ? space.getSpecialty().getUuid() : null)
                        .specialtyName(space.getSpecialty() != null ? space.getSpecialty().getName() : null)
                        .capacity(space.getCapacity())
                        .dayOfWeek(slot.getDayOfWeek())
                        .timeSlotUuid(slot.getTimeSlotUuid())
                        .availableTeachingHourUuids(teachingHourUuids)
                        .isPreferred(space.getSpecialty() != null &&
                                space.getSpecialty().getUuid().equals(course.getPreferredSpecialtyUuid()))
                        .build();

                availableSpaces.add(spaceSlot);
            }
        }

        return availableSpaces;
    }

    private GeneratedClassSessionDTO createSessionFromCandidate(
            AssignmentCandidateDTO candidate,
            GroupRequirementDTO group,
            CourseRequirementDTO course,
            String sessionType) {

        // Obtener nombres de entidades
        TeacherEntity teacher = teacherService.findTeacherOrThrow(candidate.getTeacherUuid());
        LearningSpaceEntity space = learningSpaceService.findOrThrow(candidate.getSpaceUuid());
        TimeSlotEntity timeSlot = timeSlotService.findOrThrow(candidate.getTimeSlotUuid());

        // Crear rangos de horas
        List<String> hourRanges = candidate.getTeachingHourUuids().stream()
                .map(uuid -> teachingHourRepository.findById(uuid).orElse(null))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(TeachingHourEntity::getOrderInTimeSlot))
                .map(hour -> hour.getStartTime().toString().substring(0, 5) + "-" +
                        hour.getEndTime().toString().substring(0, 5))
                .collect(Collectors.toList());

        return GeneratedClassSessionDTO.builder()
                .courseName(course.getCourseName())
                .groupName(group.getGroupName())
                .teacherName(teacher.getFullName())
                .learningSpaceName(space.getName())
                .dayOfWeek(candidate.getDayOfWeek())
                .timeSlotName(timeSlot.getName())
                .teachingHourRanges(hourRanges)
                .sessionType(sessionType)
                .notes("Generado automáticamente")
                .isNewlyGenerated(true)
                .build();
    }

    private boolean createRealClassSession(AssignmentCandidateDTO candidate, UUID periodUuid, ScheduleGenerationContext context) {
        try {
            // Pre-validar antes de intentar crear
            if (!preValidateCandidate(candidate, periodUuid)) {
                log.warn("⚠️ Candidato no válido para crear sesión: {}", candidate);
                return false;
            }

            // Obtener tipo de sesión
            TeachingTypeEntity sessionType = teachingTypeService.getAllTeachingTypes().stream()
                    .filter(type -> type.getName().equals(candidate.getSessionType()))
                    .findFirst()
                    .map(dto -> teachingTypeService.findTeachingTypeOrThrow(dto.getUuid()))
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de sesión no encontrado: " + candidate.getSessionType()));

            // Crear DTO para ClassSessionService
            ClassSessionRequestDTO sessionRequest = new ClassSessionRequestDTO();
            sessionRequest.setStudentGroupUuid(candidate.getGroupUuid());
            sessionRequest.setCourseUuid(candidate.getCourseUuid());
            sessionRequest.setTeacherUuid(candidate.getTeacherUuid());
            sessionRequest.setLearningSpaceUuid(candidate.getSpaceUuid());
            sessionRequest.setDayOfWeek(candidate.getDayOfWeek());
            sessionRequest.setSessionTypeUuid(sessionType.getUuid());
            sessionRequest.setTeachingHourUuids(candidate.getTeachingHourUuids());
            sessionRequest.setNotes("Generado automáticamente");

            // Crear la sesión
            ClassSessionResponseDTO createdSession = classSessionService.createClassSession(sessionRequest);
            candidate.setScore((double) createdSession.getUuid().hashCode()); // Guardar referencia del UUID

            log.debug("✅ Sesión creada exitosamente: {}", createdSession.getUuid());
            return true;

        } catch (IllegalArgumentException e) {
            // Errores de validación - no son fatales
            log.warn("⚠️ Error de validación al crear sesión: {}", e.getMessage());
            context.addWarning("Conflicto de validación", e.getMessage(), "MEDIUM");
            return false;

        } catch (Exception e) {
            // Otros errores - registrar pero no fallar
            log.error("❌ Error inesperado al crear sesión: {}", e.getMessage());
            context.addWarning("Error de creación", "No se pudo crear sesión: " + e.getMessage(), "HIGH");
            return false;
        }
    }
    /**
     * Pre-validación del candidato antes de crear la sesión
     */
    private boolean preValidateCandidate(AssignmentCandidateDTO candidate, UUID periodUuid) {
        try {
            // 1. Verificar que las horas pedagógicas sean consecutivas
            List<TeachingHourEntity> teachingHours = candidate.getTeachingHourUuids().stream()
                    .map(uuid -> teachingHourRepository.findById(uuid).orElse(null))
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(TeachingHourEntity::getOrderInTimeSlot))
                    .collect(Collectors.toList());

            if (!areConsecutive(teachingHours)) {
                log.warn("⚠️ Horas pedagógicas no consecutivas: {}", candidate.getTeachingHourUuids());
                return false;
            }

            // 2. Verificar conflictos de docente
            List<ClassSessionEntity> teacherConflicts = classSessionRepository.findTeacherConflicts(
                    candidate.getTeacherUuid(),
                    candidate.getDayOfWeek(),
                    periodUuid,
                    candidate.getTeachingHourUuids()
            );

            if (!teacherConflicts.isEmpty()) {
                log.warn("⚠️ Conflicto de docente detectado: {} sesiones en conflicto", teacherConflicts.size());
                return false;
            }

            // 3. Verificar conflictos de aula
            List<ClassSessionEntity> spaceConflicts = classSessionRepository.findLearningSpaceConflicts(
                    candidate.getSpaceUuid(),
                    candidate.getDayOfWeek(),
                    periodUuid,
                    candidate.getTeachingHourUuids()
            );

            if (!spaceConflicts.isEmpty()) {
                log.warn("⚠️ Conflicto de aula detectado: {} sesiones en conflicto", spaceConflicts.size());
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("❌ Error en pre-validación: {}", e.getMessage());
            return false;
        }
    }


    // ===================== MÉTODOS DE CONTEXTO Y SEGUIMIENTO =====================

    private boolean checkTimeContinuity(AssignmentCandidateDTO candidate, ScheduleGenerationContext context) {
        // Verificar si hay sesiones adyacentes para el mismo grupo
        // Implementación simplificada - en producción sería más sofisticada
        return context.hasAdjacentSession(candidate.getGroupUuid(), candidate.getDayOfWeek(),
                candidate.getTimeSlotUuid());
    }

    private double calculateDistributionBonus(AssignmentCandidateDTO candidate, ScheduleGenerationContext context) {
        // Calcular bonus basado en qué tan equilibrada queda la distribución
        int currentDayHours = getDailyHoursForGroup(candidate.getGroupUuid(), candidate.getDayOfWeek(), context);
        int averageDesiredHours = context.getAverageHoursPerDay(candidate.getGroupUuid());

        // Bonus si nos acerca al promedio deseado
        int difference = Math.abs(currentDayHours - averageDesiredHours);
        return Math.max(0, 15 - difference * 2); // Máximo 15 puntos bonus
    }

    private int getDailyHoursForGroup(UUID groupUuid, DayOfWeek dayOfWeek, ScheduleGenerationContext context) {
        return context.getDailyHours(groupUuid, dayOfWeek);
    }

    private void markSlotAsOccupied(List<ScheduleSlotDTO> availableSlots, AssignmentCandidateDTO candidate) {
        // Remover las horas específicas que fueron asignadas
        availableSlots.forEach(slot -> {
            if (slot.getDayOfWeek().equals(candidate.getDayOfWeek()) &&
                    slot.getTimeSlotUuid().equals(candidate.getTimeSlotUuid())) {

                // Remover solo las horas pedagógicas específicas que se usaron
                List<UUID> remainingHours = new ArrayList<>(slot.getTeachingHourUuids());
                remainingHours.removeAll(candidate.getTeachingHourUuids());
                slot.setTeachingHourUuids(remainingHours);
            }
        });

        // Remover slots que ya no tienen suficientes horas consecutivas
        availableSlots.removeIf(slot -> {
            List<UUID> consecutiveHours = getConsecutiveHours(slot, 1); // Al menos 1 hora
            return consecutiveHours.isEmpty();
        });
    }

    private void updateAvailableSlots(List<ScheduleSlotDTO> availableSlots, List<GeneratedClassSessionDTO> sessions) {
        // Actualizar slots disponibles removiendo los que ya están ocupados
        for (GeneratedClassSessionDTO session : sessions) {
            availableSlots.removeIf(slot ->
                    slot.getDayOfWeek().equals(session.getDayOfWeek()) &&
                            slot.getTimeSlotName().equals(session.getTimeSlotName())
            );
        }
    }

    private void updateContextWithGeneratedSessions(ScheduleGenerationContext context, List<GeneratedClassSessionDTO> sessions) {
        for (GeneratedClassSessionDTO session : sessions) {
            context.addGeneratedSession(session);
        }
    }

    private UUID getTeacherUuidFromSessionName(String teacherName) {
        // Método auxiliar para obtener UUID del docente desde su nombre
        // En un escenario real, se mantendría un mapa de nombres a UUIDs
        try {
            List<TeacherResponseDTO> teachers = teacherService.getAllTeachers();
            return teachers.stream()
                    .filter(teacher -> teacher.getFullName().equals(teacherName))
                    .map(TeacherResponseDTO::getUuid)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.warn("No se pudo obtener UUID del docente: {}", teacherName);
            return null;
        }
    }

    // ===================== MÉTODOS DE ANÁLISIS Y RESULTADO =====================

    private ScheduleConstraintsDTO analyzeConstraints(List<GroupRequirementDTO> requirements) {
        int totalGroups = requirements.size();
        int totalCourses = requirements.stream()
                .mapToInt(group -> group.getCourses().size())
                .sum();
        int totalRequiredHours = requirements.stream()
                .mapToInt(GroupRequirementDTO::getTotalWeeklyHours)
                .sum();

        // Análisis de recursos disponibles
        int availableTeachers = teacherService.getAllTeachers().size();
        int availableSpaces = learningSpaceService.getAllLearningSpaces().size();
        int availableTimeSlots = timeSlotService.getAllTimeSlots().size() * 6; // 6 días por semana

        List<String> constraints = new ArrayList<>();
        if (totalRequiredHours > availableTimeSlots * 4) { // Asumiendo 4 horas promedio por slot
            constraints.add("Insuficientes slots de tiempo para todas las horas requeridas");
        }
        if (totalCourses > availableTeachers * 3) { // Asumiendo 3 cursos promedio por docente
            constraints.add("Posible insuficiencia de docentes especializados");
        }

        return ScheduleConstraintsDTO.builder()
                .totalGroups(totalGroups)
                .totalCourses(totalCourses)
                .totalRequiredHours(totalRequiredHours)
                .availableTeachers(availableTeachers)
                .availableSpaces(availableSpaces)
                .availableTimeSlots(availableTimeSlots)
                .potentialConstraints(constraints)
                .build();
    }

    private ScheduleFeasibilityDTO analyzeFeasibility(List<GroupRequirementDTO> requirements, ScheduleConstraintsDTO constraints) {
        List<String> challenges = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        // Calcular puntuación de factibilidad
        double feasibilityScore = 1.0;

        // Factor 1: Ratio horas/slots disponibles
        double hourSlotRatio = (double) constraints.getTotalRequiredHours() / (constraints.getAvailableTimeSlots() * 2);
        if (hourSlotRatio > 0.8) {
            feasibilityScore -= 0.3;
            challenges.add("Alta densidad de horas requeridas vs. slots disponibles");
            recommendations.add("Considere aumentar el número de turnos o redistribuir cursos");
        }

        // Factor 2: Disponibilidad de docentes especializados
        double teacherCourseRatio = (double) constraints.getTotalCourses() / constraints.getAvailableTeachers();
        if (teacherCourseRatio > 4) {
            feasibilityScore -= 0.2;
            challenges.add("Posible sobrecarga de docentes");
            recommendations.add("Verifique que todos los docentes tengan las competencias requeridas");
        }

        // Factor 3: Complejidad de coordinación
        if (constraints.getTotalGroups() > 10) {
            feasibilityScore -= 0.1;
            challenges.add("Alta complejidad de coordinación con muchos grupos");
            recommendations.add("Considere generar horarios por fases o carreras");
        }

        boolean isFeasible = feasibilityScore > 0.5 && challenges.size() < 3;

        if (isFeasible) {
            recommendations.add("El sistema tiene buena capacidad para generar horarios automáticamente");
        } else {
            recommendations.add("Se recomienda revisar recursos y configuración antes de la generación");
        }

        return ScheduleFeasibilityDTO.builder()
                .isFeasible(isFeasible)
                .feasibilityScore(Math.max(0, feasibilityScore))
                .challenges(challenges)
                .recommendations(recommendations)
                .build();
    }

    private ScheduleGenerationResultDTO buildResult(
            ScheduleGenerationContext context,
            List<GeneratedClassSessionDTO> generatedSessions,
            long executionTime) {

        // Construir resumen
        ScheduleGenerationSummaryDTO summary = ScheduleGenerationSummaryDTO.builder()
                .totalGroupsProcessed(context.getGroupRequirements().size())
                .totalCoursesProcessed(context.getGroupRequirements().stream()
                        .mapToInt(group -> group.getCourses().size()).sum())
                .totalSessionsGenerated(generatedSessions.size())
                .totalHoursAssigned(generatedSessions.size()) // Simplificado
                .conflictsFound(context.getConflicts().size())
                .warningsGenerated(context.getWarnings().size())
                .successRate(calculateSuccessRate(context, generatedSessions))
                .build();

        // Construir estadísticas
        ScheduleStatisticsDTO statistics = buildStatistics(generatedSessions, context);

        return ScheduleGenerationResultDTO.builder()
                .success(summary.getConflictsFound() == 0)
                .message(summary.getConflictsFound() == 0 ?
                        "Horarios generados exitosamente" :
                        "Horarios generados con " + summary.getConflictsFound() + " conflictos")
                .summary(summary)
                .generatedSessions(generatedSessions)
                .conflicts(context.getConflicts())
                .warnings(context.getWarnings())
                .statistics(statistics)
                .executionTimeMs(executionTime)
                .build();
    }

    private double calculateSuccessRate(ScheduleGenerationContext context, List<GeneratedClassSessionDTO> sessions) {
        int totalRequiredSessions = context.getGroupRequirements().stream()
                .mapToInt(group -> group.getCourses().stream()
                        .mapToInt(CourseRequirementDTO::getTotalHours)
                        .sum())
                .sum();

        return totalRequiredSessions > 0 ?
                (double) sessions.size() / totalRequiredSessions : 1.0;
    }

    private ScheduleStatisticsDTO buildStatistics(List<GeneratedClassSessionDTO> sessions, ScheduleGenerationContext context) {
        Map<String, Integer> sessionsPerDay = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getDayOfWeek().name(),
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

        Map<String, Integer> sessionsPerTimeSlot = sessions.stream()
                .collect(Collectors.groupingBy(
                        GeneratedClassSessionDTO::getTimeSlotName,
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

        double averageHoursPerDay = sessions.size() / 6.0; // 6 días de la semana

        return ScheduleStatisticsDTO.builder()
                .sessionsPerDay(sessionsPerDay)
                .sessionsPerTimeSlot(sessionsPerTimeSlot)
                .teacherUtilization(new HashMap<>()) // Por implementar
                .spaceUtilization(new HashMap<>()) // Por implementar
                .hoursPerCourse(new HashMap<>()) // Por implementar
                .averageHoursPerDay(averageHoursPerDay)
                .distributionBalance(calculateDistributionBalance(sessionsPerDay))
                .build();
    }

    private double calculateDistributionBalance(Map<String, Integer> sessionsPerDay) {
        if (sessionsPerDay.isEmpty()) return 1.0;

        double average = sessionsPerDay.values().stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = sessionsPerDay.values().stream()
                .mapToDouble(count -> Math.pow(count - average, 2))
                .average().orElse(0);

        // Convertir varianza a una puntuación de balance (0-1, donde 1 es perfectamente balanceado)
        return Math.max(0, 1 - (variance / (average + 1)));
    }

    /**
     * Analiza el estado actual de los horarios antes de la generación
     */
    public ExistingScheduleAnalysisDTO analyzeExistingSchedules(ScheduleGenerationRequestDTO request) {
        log.info("🔍 Analizando horarios existentes antes de la generación");

        List<StudentGroupEntity> targetGroups = getTargetGroups(request);
        List<UUID> groupUuids = targetGroups.stream()
                .map(StudentGroupEntity::getUuid)
                .collect(Collectors.toList());

        // Análisis básico de horarios existentes
        Map<String, Object> basicAnalysis = scheduleUtilityService
                .analyzeExistingSchedules(request.getPeriodUuid(), groupUuids);

        // Análisis de carga de trabajo
        Map<String, Object> workloadData = scheduleUtilityService
                .analyzeWorkloadAndResources(request.getPeriodUuid(), groupUuids);

        // Convertir a DTOs estructurados
        List<GroupScheduleStatusDTO> groupStatuses = convertToGroupStatusDTOs(
                (List<Map<String, Object>>) basicAnalysis.get("groupDetails"), targetGroups);

        WorkloadAnalysisDTO workloadAnalysis = convertToWorkloadAnalysisDTO(workloadData);

        return ExistingScheduleAnalysisDTO.builder()
                .groupStatuses(groupStatuses)
                .totalGroups(safeToInteger(basicAnalysis.get("totalGroups")))
                .groupsWithExistingSessions(safeToInteger(basicAnalysis.get("groupsWithExistingSessions")))
                .groupsWithoutSessions(safeToInteger(basicAnalysis.get("groupsWithoutSessions")))
                .needsUserDecision((Boolean) basicAnalysis.get("needsUserDecision"))
                .recommendations((List<String>) basicAnalysis.get("recommendations"))
                .workloadAnalysis(workloadAnalysis)
                .build();
    }


    /**
     * Método helper para convertir de manera segura cualquier Number a Integer
     * Maneja tanto Long como Integer que pueden venir de SQL Server
     */
    private Integer safeToInteger(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Long) {
            Long longValue = (Long) value;
            if (longValue > Integer.MAX_VALUE) {
                log.warn("⚠️ Valor Long {} excede Integer.MAX_VALUE, truncando", longValue);
                return Integer.MAX_VALUE;
            }
            return longValue.intValue();
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        log.warn("⚠️ Tipo inesperado para conversión a Integer: {}", value.getClass());
        return 0;
    }
    /**
     * Limpia horarios existentes según la estrategia especificada
     */
    @Transactional
    public ScheduleCleanupResultDTO cleanupExistingSchedules(ScheduleCleanupRequestDTO request) {
        log.info("🧹 Ejecutando limpieza de horarios: estrategia {}", request.getStrategy());

        try {
            Map<String, Object> cleanupResult;
            List<String> warnings = new ArrayList<>();

            switch (request.getStrategy()) {
                case RESET_ALL:
                    cleanupResult = scheduleUtilityService.selectiveClearSchedules(
                            request.getPeriodUuid(), request.getGroupUuids(), true);
                    break;

                case SELECTIVE_CLEANUP:
                    cleanupResult = scheduleUtilityService.selectiveClearSchedules(
                            request.getPeriodUuid(), request.getGroupUuids(), false);
                    warnings.add("Se eliminaron solo sesiones problemáticas o incompletas");
                    break;

                case COMPLETE_EXISTING:
                    // No eliminar nada, solo marcar para completar
                    cleanupResult = Map.of(
                            "deletedSessions", 0,
                            "affectedGroups", 0,
                            "affectedCourses", 0,
                            "cleanupStrategy", "COMPLETE_EXISTING"
                    );
                    warnings.add("Se mantendrán horarios existentes y se completarán los faltantes");
                    break;

                default:
                    throw new IllegalArgumentException("Estrategia de limpieza no válida: " + request.getStrategy());
            }

            return ScheduleCleanupResultDTO.builder()
                    .success(true)
                    .message("Limpieza completada exitosamente")
                    .deletedSessions((Integer) cleanupResult.get("deletedSessions"))
                    .affectedGroups((Integer) cleanupResult.get("affectedGroups"))
                    .affectedCourses((Integer) cleanupResult.get("affectedCourses"))
                    .cleanupStrategy((String) cleanupResult.get("cleanupStrategy"))
                    .warnings(warnings)
                    .details(cleanupResult)
                    .build();

        } catch (Exception e) {
            log.error("❌ Error en limpieza de horarios: {}", e.getMessage(), e);
            return ScheduleCleanupResultDTO.builder()
                    .success(false)
                    .message("Error en la limpieza: " + e.getMessage())
                    .warnings(Arrays.asList("Se produjo un error durante la limpieza"))
                    .build();
        }
    }

    /**
     * Generación inteligente que considera horarios existentes
     */
    @Transactional
    public ScheduleGenerationResultDTO generateScheduleIntelligent(
            ScheduleGenerationRequestDTO request,
            ScheduleCleanupRequestDTO.CleanupStrategy strategy) {

        log.info("🧠 Iniciando generación inteligente con estrategia: {}", strategy);

        try {
            // 1. Analizar estado actual
            ExistingScheduleAnalysisDTO existingAnalysis = analyzeExistingSchedules(request);

            // 2. Aplicar limpieza si es necesario
            if (existingAnalysis.getNeedsUserDecision() &&
                    strategy != ScheduleCleanupRequestDTO.CleanupStrategy.COMPLETE_EXISTING) {

                List<UUID> groupUuids = getTargetGroups(request).stream()
                        .map(StudentGroupEntity::getUuid)
                        .collect(Collectors.toList());

                ScheduleCleanupRequestDTO cleanupRequest = ScheduleCleanupRequestDTO.builder()
                        .periodUuid(request.getPeriodUuid())
                        .groupUuids(groupUuids)
                        .strategy(strategy)
                        .confirmOverwrite(true)
                        .build();

                ScheduleCleanupResultDTO cleanupResult = cleanupExistingSchedules(cleanupRequest);
                if (!cleanupResult.getSuccess()) {
                    return ScheduleGenerationResultDTO.builder()
                            .success(false)
                            .message("Error en la limpieza previa: " + cleanupResult.getMessage())
                            .build();
                }
            }

            // 3. Proceder con la generación normal
            ScheduleGenerationResultDTO result = generateSchedule(request);

            // 4. Agregar información del contexto de limpieza
            if (result.isSuccess()) {
                String originalMessage = result.getMessage();
                String strategyInfo = getStrategyDescription(strategy);
                result.setMessage(originalMessage + " (" + strategyInfo + ")");
            }

            return result;

        } catch (Exception e) {
            log.error("❌ Error en generación inteligente: {}", e.getMessage(), e);
            return ScheduleGenerationResultDTO.builder()
                    .success(false)
                    .message("Error en la generación inteligente: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Optimización específica del algoritmo de asignación
     */

    private AssignmentCandidateDTO findBestAssignmentSlotOptimized(
            GroupRequirementDTO group,
            CourseRequirementDTO course,
            String sessionType,
            int requiredHours,
            List<ScheduleSlotDTO> availableSlots,
            ScheduleGenerationContext context,
            UUID preferredTeacher) {

        // Verificar si ya hay sesiones existentes para este curso/grupo
        List<ClassSessionEntity> existingSessions = classSessionRepository
                .findByStudentGroupUuidAndPeriod(group.getGroupUuid(), context.getRequest().getPeriodUuid())
                .stream()
                .filter(session -> session.getCourse().getUuid().equals(course.getCourseUuid()))
                .collect(Collectors.toList());

        List<AssignmentCandidateDTO> candidates = new ArrayList<>();

        for (ScheduleSlotDTO slot : availableSlots) {
            // Saltar días excluidos
            if (context.getRequest().getExcludedDays().contains(slot.getDayOfWeek())) {
                continue;
            }

            // Priorizar continuidad temporal si hay sesiones existentes
            double continuityBonus = 0.0;
            if (!existingSessions.isEmpty()) {
                continuityBonus = calculateContinuityBonus(slot, existingSessions, context);
            }

            // Verificar disponibilidad de horas consecutivas
            List<UUID> availableHours = getConsecutiveHours(slot, requiredHours);
            if (availableHours.size() < requiredHours) {
                continue;
            }

            // Buscar docentes disponibles (priorizar docente ya asignado al curso)
            UUID courseTeacherKey = getCourseTeacherFromExisting(existingSessions);
            UUID actualPreferredTeacher = courseTeacherKey != null ? courseTeacherKey : preferredTeacher;

            List<TeacherAvailabilitySlotDTO> availableTeachers =
                    getAvailableTeachers(course, slot, availableHours, actualPreferredTeacher);

            if (availableTeachers.isEmpty()) {
                continue;
            }

            // Buscar aulas disponibles
            List<SpaceAvailabilitySlotDTO> availableSpaces =
                    getAvailableSpaces(course, sessionType, slot, availableHours);

            if (availableSpaces.isEmpty()) {
                continue;
            }

            // Crear candidatos con bonus de continuidad
            for (TeacherAvailabilitySlotDTO teacher : availableTeachers) {
                for (SpaceAvailabilitySlotDTO space : availableSpaces) {
                    AssignmentCandidateDTO candidate = AssignmentCandidateDTO.builder()
                            .groupUuid(group.getGroupUuid())
                            .courseUuid(course.getCourseUuid())
                            .teacherUuid(teacher.getTeacherUuid())
                            .spaceUuid(space.getSpaceUuid())
                            .dayOfWeek(slot.getDayOfWeek())
                            .timeSlotUuid(slot.getTimeSlotUuid())
                            .teachingHourUuids(availableHours)
                            .sessionType(sessionType)
                            .build();

                    // Calcular puntuación con bonus de continuidad
                    double baseScore = calculateAssignmentScore(candidate, slot, teacher, space, context);
                    double finalScore = baseScore + continuityBonus;
                    candidate.setScore(finalScore);

                    candidates.add(candidate);
                }
            }
        }

        // Ordenar por puntuación y devolver el mejor
        return candidates.stream()
                .max(Comparator.comparing(AssignmentCandidateDTO::getScore))
                .orElse(null);
    }

// Métodos auxiliares

    private List<GroupScheduleStatusDTO> convertToGroupStatusDTOs(
            List<Map<String, Object>> groupDetails,
            List<StudentGroupEntity> targetGroups) {

        Map<UUID, String> groupNames = targetGroups.stream()
                .collect(Collectors.toMap(
                        StudentGroupEntity::getUuid,
                        StudentGroupEntity::getName
                ));

        return groupDetails.stream()
                .map(details -> {
                    UUID groupUuid = (UUID) details.get("groupUuid");
                    Boolean hasExisting = (Boolean) details.get("hasExistingSessions");
                    Double completeness = (Double) details.getOrDefault("estimatedCompleteness", 0.0);

                    String recommendedAction = "NONE";
                    if (hasExisting) {
                        if (completeness < 0.3) {
                            recommendedAction = "RESET";
                        } else if (completeness < 0.8) {
                            recommendedAction = "COMPLETE";
                        } else {
                            recommendedAction = "NONE";
                        }
                    }

                    return GroupScheduleStatusDTO.builder()
                            .groupUuid(groupUuid)
                            .groupName(groupNames.get(groupUuid))
                            .hasExistingSessions(hasExisting)
                            .sessionCount((Integer) details.getOrDefault("sessionCount", 0))
                            .totalAssignedHours((Integer) details.getOrDefault("totalAssignedHours", 0))
                            .assignedCoursesCount((Integer) details.getOrDefault("assignedCoursesCount", 0))
                            .assignedTeachersCount((Integer) details.getOrDefault("assignedTeachersCount", 0))
                            .estimatedCompleteness(completeness)
                            .distributionByDay((Map<String, Integer>) details.getOrDefault("distributionByDay", new HashMap<>()))
                            .recommendedAction(recommendedAction)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private WorkloadAnalysisDTO convertToWorkloadAnalysisDTO(Map<String, Object> workloadData) {
        Double avgTeacherLoad = (Double) workloadData.get("averageTeacherLoad");
        Double avgSpaceLoad = (Double) workloadData.get("averageSpaceLoad");
        Double utilization = (Double) workloadData.get("systemUtilization");

        String utilizationLevel;
        if (utilization < 0.3) utilizationLevel = "LOW";
        else if (utilization < 0.6) utilizationLevel = "MEDIUM";
        else if (utilization < 0.8) utilizationLevel = "HIGH";
        else utilizationLevel = "CRITICAL";

        return WorkloadAnalysisDTO.builder()
                .averageTeacherLoad(avgTeacherLoad)
                .averageSpaceLoad(avgSpaceLoad)
                .systemUtilization(utilization)
                .overloadedTeachers((List<String>) workloadData.get("overloadedTeachers"))
                .overloadedSpaces((List<String>) workloadData.get("overloadedSpaces"))
                .recommendations((List<String>) workloadData.get("recommendations"))
                .utilizationLevel(utilizationLevel)
                .build();
    }

    private double calculateContinuityBonus(ScheduleSlotDTO slot, List<ClassSessionEntity> existingSessions, ScheduleGenerationContext context) {
        // Bonus por continuidad temporal (mismo día, turno adyacente)
        double bonus = 0.0;

        for (ClassSessionEntity existing : existingSessions) {
            if (existing.getDayOfWeek().equals(slot.getDayOfWeek())) {
                // Mismo día, verificar si es turno adyacente
                String existingTimeSlot = existing.getTeachingHours().stream()
                        .findFirst()
                        .map(th -> th.getTimeSlot().getName())
                        .orElse("");

                if (isAdjacentTimeSlot(existingTimeSlot, slot.getTimeSlotName())) {
                    bonus += 25.0; // Bonus alto por continuidad
                } else {
                    bonus += 10.0; // Bonus menor por mismo día
                }
            }
        }

        return bonus;
    }

    private UUID getCourseTeacherFromExisting(List<ClassSessionEntity> existingSessions) {
        return existingSessions.stream()
                .map(session -> session.getTeacher().getUuid())
                .findFirst()
                .orElse(null);
    }

    private boolean isAdjacentTimeSlot(String timeSlot1, String timeSlot2) {
        // Lógica simplificada para determinar si dos turnos son adyacentes
        // Ejemplo: M1 y M2 son adyacentes, T1 y T2 son adyacentes

        // Mapeo de turnos adyacentes comunes
        Map<String, List<String>> adjacentSlots = Map.of(
                "M1", Arrays.asList("M2"),
                "M2", Arrays.asList("M1", "M3"),
                "M3", Arrays.asList("M2", "T1"),
                "T1", Arrays.asList("M3", "T2"),
                "T2", Arrays.asList("T1", "N1"),
                "N1", Arrays.asList("T2")
        );

        return adjacentSlots.getOrDefault(timeSlot1, new ArrayList<>()).contains(timeSlot2);
    }

    private String getStrategyDescription(ScheduleCleanupRequestDTO.CleanupStrategy strategy) {
        switch (strategy) {
            case RESET_ALL:
                return "Se eliminaron todos los horarios existentes";
            case SELECTIVE_CLEANUP:
                return "Se eliminaron solo horarios problemáticos";
            case COMPLETE_EXISTING:
                return "Se mantuvieron horarios existentes";
            default:
                return "Estrategia no especificada";
        }
    }

}

