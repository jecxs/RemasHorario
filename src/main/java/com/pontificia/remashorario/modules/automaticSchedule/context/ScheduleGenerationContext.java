package com.pontificia.remashorario.modules.automaticSchedule.context;


import com.pontificia.remashorario.modules.automaticSchedule.dto.*;
import com.pontificia.remashorario.modules.classSession.ClassSessionEntity;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
public class ScheduleGenerationContext {

    private final ScheduleGenerationRequestDTO request;
    private final List<GroupRequirementDTO> groupRequirements;

    // Estado de la generación
    private final List<ScheduleConflictDTO> conflicts = new ArrayList<>();
    private List<ScheduleWarningDTO> warnings = new ArrayList<>();
    private final List<GeneratedClassSessionDTO> generatedSessions = new ArrayList<>();

    // Mapas de seguimiento para optimización
    private final Map<String, Integer> dailyHoursPerGroup = new HashMap<>(); // "groupUuid_dayOfWeek" -> hours
    private final Map<String, Set<UUID>> occupiedSlots = new HashMap<>(); // "dayOfWeek_timeSlotUuid" -> Set<teachingHourUuids>
    private final Map<UUID, Set<String>> teacherAssignments = new HashMap<>(); // teacherUuid -> Set<"dayOfWeek_timeSlot">
    private final Map<UUID, Set<String>> spaceAssignments = new HashMap<>(); // spaceUuid -> Set<"dayOfWeek_timeSlot">

    // Configuración calculada
    private final Map<UUID, Integer> totalHoursPerGroup = new HashMap<>();
    private final Map<UUID, Double> averageHoursPerDayPerGroup = new HashMap<>();

    // Mapas adicionales para tracking inteligente
    private final Map<String, UUID> courseTeacherAssignments = new HashMap<>(); // "groupUuid_courseUuid" -> teacherUuid
    private final Map<UUID, List<ClassSessionEntity>> existingSessions = new HashMap<>(); // groupUuid -> existing sessions
    private final Set<String> preservedSlots = new HashSet<>(); // "dayOfWeek_timeSlot_teachingHour" para slots a preservar

    public ScheduleGenerationContext(ScheduleGenerationRequestDTO request,
                                     List<GroupRequirementDTO> groupRequirements) {
        this.request = request;
        this.groupRequirements = groupRequirements;

        // Calcular horas totales y promedio por grupo
        calculateGroupMetrics();
    }

    private void calculateGroupMetrics() {
        for (GroupRequirementDTO group : groupRequirements) {
            totalHoursPerGroup.put(group.getGroupUuid(), group.getTotalWeeklyHours());

            // Calcular promedio considerando días excluidos
            int workingDays = 6 - request.getExcludedDays().size(); // Lunes a Sábado menos excluidos
            double averageHours = workingDays > 0 ? (double) group.getTotalWeeklyHours() / workingDays : 0;
            averageHoursPerDayPerGroup.put(group.getGroupUuid(), averageHours);
        }
    }

    // ===================== MÉTODOS DE SEGUIMIENTO =====================

    /**
     * Registra una nueva sesión generada y actualiza el estado del contexto
     */
    public void addGeneratedSession(GeneratedClassSessionDTO session) {
        generatedSessions.add(session);

        // Actualizar horas diarias por grupo
        String dayKey = session.getGroupName() + "_" + session.getDayOfWeek().name();
        dailyHoursPerGroup.merge(dayKey, session.getTeachingHourRanges().size(), Integer::sum);

        // Marcar slots como ocupados
        String slotKey = session.getDayOfWeek().name() + "_" + session.getTimeSlotName();
        occupiedSlots.computeIfAbsent(slotKey, k -> new HashSet<>())
                .addAll(convertHourRangesToUuids(session.getTeachingHourRanges()));

        // Registrar asignaciones de docente y aula
        String timeSlotKey = session.getDayOfWeek().name() + "_" + session.getTimeSlotName();
        UUID teacherUuid = getTeacherUuidByName(session.getTeacherName());
        UUID spaceUuid = getSpaceUuidByName(session.getLearningSpaceName());

        if (teacherUuid != null) {
            teacherAssignments.computeIfAbsent(teacherUuid, k -> new HashSet<>()).add(timeSlotKey);
        }
        if (spaceUuid != null) {
            spaceAssignments.computeIfAbsent(spaceUuid, k -> new HashSet<>()).add(timeSlotKey);
        }
    }

    /**
     * Añade un conflicto al contexto
     */
    public void addConflict(ScheduleConflictDTO conflict) {
        conflicts.add(conflict);
    }

    /**
     * Añade una advertencia al contexto
     */
    public void addWarning(String type, String message, String severity) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }

        ScheduleWarningDTO warning = ScheduleWarningDTO.builder()
                .type(type)
                .message(message)
                .severity(severity)
                .build();

        this.warnings.add(warning);
    }

    /**
     * Obtiene las horas asignadas para un grupo en un día específico
     */
    public int getDailyHours(UUID groupUuid, DayOfWeek dayOfWeek) {
        String key = groupUuid + "_" + dayOfWeek.name();
        return dailyHoursPerGroup.getOrDefault(key, 0);
    }

    /**
     * Obtiene el promedio de horas por día para un grupo
     */
    public int getAverageHoursPerDay(UUID groupUuid) {
        return averageHoursPerDayPerGroup.getOrDefault(groupUuid, 0.0).intValue();
    }

    /**
     * Verifica si un slot está disponible
     */
    public boolean isSlotAvailable(DayOfWeek dayOfWeek, String timeSlotName, List<UUID> teachingHourUuids) {
        String slotKey = dayOfWeek.name() + "_" + timeSlotName;
        Set<UUID> occupiedHours = occupiedSlots.getOrDefault(slotKey, new HashSet<>());

        return teachingHourUuids.stream().noneMatch(occupiedHours::contains);
    }

    /**
     * Verifica si un docente está disponible en un slot específico
     */
    public boolean isTeacherAvailable(UUID teacherUuid, DayOfWeek dayOfWeek, String timeSlotName) {
        String timeSlotKey = dayOfWeek.name() + "_" + timeSlotName;
        Set<String> assignments = teacherAssignments.getOrDefault(teacherUuid, new HashSet<>());

        return !assignments.contains(timeSlotKey);
    }

    /**
     * Verifica si un aula está disponible en un slot específico
     */
    public boolean isSpaceAvailable(UUID spaceUuid, DayOfWeek dayOfWeek, String timeSlotName) {
        String timeSlotKey = dayOfWeek.name() + "_" + timeSlotName;
        Set<String> assignments = spaceAssignments.getOrDefault(spaceUuid, new HashSet<>());

        return !assignments.contains(timeSlotKey);
    }

    /**
     * Verifica si existe una sesión adyacente para continuidad
     */
    public boolean hasAdjacentSession(UUID groupUuid, DayOfWeek dayOfWeek, UUID timeSlotUuid) {
        // Buscar sesiones del mismo grupo en el mismo día
        return generatedSessions.stream()
                .anyMatch(session -> {
                    UUID sessionGroupUuid = getGroupUuidByName(session.getGroupName());
                    return sessionGroupUuid != null &&
                            sessionGroupUuid.equals(groupUuid) &&
                            session.getDayOfWeek().equals(dayOfWeek);
                });
    }

    /**
     * Obtiene el total de horas asignadas para un grupo
     */
    public int getTotalAssignedHours(UUID groupUuid) {
        return generatedSessions.stream()
                .filter(session -> {
                    UUID sessionGroupUuid = getGroupUuidByName(session.getGroupName());
                    return sessionGroupUuid != null && sessionGroupUuid.equals(groupUuid);
                })
                .mapToInt(session -> session.getTeachingHourRanges().size())
                .sum();
    }

    /**
     * Obtiene el porcentaje de progreso para un grupo específico
     */
    public double getGroupProgress(UUID groupUuid) {
        int totalRequired = totalHoursPerGroup.getOrDefault(groupUuid, 0);
        int assigned = getTotalAssignedHours(groupUuid);

        return totalRequired > 0 ? (double) assigned / totalRequired : 0.0;
    }

    /**
     * Verifica si hay desequilibrio en la distribución diaria de un grupo
     */
    public boolean hasDistributionImbalance(UUID groupUuid) {
        if (!request.getDistributeEvenly()) {
            return false;
        }

        List<DayOfWeek> workingDays = Arrays.asList(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
        );

        // Filtrar días excluidos
        workingDays = workingDays.stream()
                .filter(day -> !request.getExcludedDays().contains(day))
                .collect(Collectors.toList());

        if (workingDays.isEmpty()) {
            return false;
        }

        // Calcular horas por día
        List<Integer> dailyHours = workingDays.stream()
                .map(day -> getDailyHours(groupUuid, day))
                .collect(Collectors.toList());

        // Calcular desviación estándar
        double average = dailyHours.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = dailyHours.stream()
                .mapToDouble(hours -> Math.pow(hours - average, 2))
                .average().orElse(0.0);
        double standardDeviation = Math.sqrt(variance);

        // Considerar desequilibrio si la desviación estándar es > 25% del promedio
        return standardDeviation > average * 0.25;
    }

    /**
     * Identifica gaps de tiempo para un grupo en un día específico
     */
    public List<String> getTimeGaps(UUID groupUuid, DayOfWeek dayOfWeek) {
        List<String> gaps = new ArrayList<>();

        // Obtener todas las sesiones del grupo en este día
        List<GeneratedClassSessionDTO> daySessions = generatedSessions.stream()
                .filter(session -> {
                    UUID sessionGroupUuid = getGroupUuidByName(session.getGroupName());
                    return sessionGroupUuid != null &&
                            sessionGroupUuid.equals(groupUuid) &&
                            session.getDayOfWeek().equals(dayOfWeek);
                })
                .sorted(Comparator.comparing(session -> session.getTeachingHourRanges().get(0)))
                .collect(Collectors.toList());

        // Analizar gaps entre sesiones consecutivas
        for (int i = 0; i < daySessions.size() - 1; i++) {
            GeneratedClassSessionDTO current = daySessions.get(i);
            GeneratedClassSessionDTO next = daySessions.get(i + 1);

            String currentEnd = current.getTeachingHourRanges().get(current.getTeachingHourRanges().size() - 1);
            String nextStart = next.getTeachingHourRanges().get(0);

            // Extraer horas de fin y inicio
            LocalTime endTime = parseTimeFromRange(currentEnd, false);
            LocalTime startTime = parseTimeFromRange(nextStart, true);

            // Si hay más de 45 minutos de diferencia, es un gap significativo
            if (Duration.between(endTime, startTime).toMinutes() > 45) {
                gaps.add(endTime.toString() + " - " + startTime.toString());
            }
        }

        return gaps;
    }

    /**
     * Obtiene estadísticas de utilización de recursos
     */
    public Map<String, Object> getResourceUtilization() {
        Map<String, Object> utilization = new HashMap<>();

        // Utilización de docentes
        Map<String, Double> teacherUtil = new HashMap<>();
        for (Map.Entry<UUID, Set<String>> entry : teacherAssignments.entrySet()) {
            double util = (double) entry.getValue().size() / (6 * 5); // 6 días, 5 turnos promedio
            teacherUtil.put(entry.getKey().toString(), Math.min(1.0, util));
        }
        utilization.put("teachers", teacherUtil);

        // Utilización de aulas
        Map<String, Double> spaceUtil = new HashMap<>();
        for (Map.Entry<UUID, Set<String>> entry : spaceAssignments.entrySet()) {
            double util = (double) entry.getValue().size() / (6 * 5); // 6 días, 5 turnos promedio
            spaceUtil.put(entry.getKey().toString(), Math.min(1.0, util));
        }
        utilization.put("spaces", spaceUtil);

        return utilization;
    }

    // ===================== MÉTODOS DE VALIDACIÓN =====================

    /**
     * Valida que no se excedan los límites máximos por día
     */
    public boolean validateDailyLimits(UUID groupUuid, DayOfWeek dayOfWeek, int additionalHours) {
        int currentHours = getDailyHours(groupUuid, dayOfWeek);
        int totalHours = currentHours + additionalHours;

        return totalHours <= request.getMaxHoursPerDay() && totalHours >= request.getMinHoursPerDay();
    }

    /**
     * Valida que se respete la continuidad del docente para un curso
     */
    public boolean validateTeacherContinuity(UUID groupUuid, UUID courseUuid, UUID proposedTeacherUuid) {
        if (!request.getRespectTeacherContinuity()) {
            return true;
        }

        // Buscar si ya hay asignaciones de este curso para este grupo
        UUID existingTeacher = generatedSessions.stream()
                .filter(session -> {
                    UUID sessionGroupUuid = getGroupUuidByName(session.getGroupName());
                    UUID sessionCourseUuid = getCourseUuidByName(session.getCourseName());
                    return sessionGroupUuid != null && sessionGroupUuid.equals(groupUuid) &&
                            sessionCourseUuid != null && sessionCourseUuid.equals(courseUuid);
                })
                .map(session -> getTeacherUuidByName(session.getTeacherName()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        return existingTeacher == null || existingTeacher.equals(proposedTeacherUuid);
    }

    /**
     * Detecta conflictos potenciales antes de crear una asignación
     */
    public List<ScheduleConflictDTO> detectConflicts(AssignmentCandidateDTO candidate) {
        List<ScheduleConflictDTO> detectedConflicts = new ArrayList<>();

        // Verificar conflicto de docente
        if (!isTeacherAvailable(candidate.getTeacherUuid(),
                candidate.getDayOfWeek(), getTimeSlotNameByUuid(candidate.getTimeSlotUuid()))) {
            detectedConflicts.add(ScheduleConflictDTO.builder()
                    .type("TEACHER_CONFLICT")
                    .severity("CRITICAL")
                    .description("El docente ya tiene una asignación en este horario")
                    .affectedTeacher(getTeacherNameByUuid(candidate.getTeacherUuid()))
                    .dayOfWeek(candidate.getDayOfWeek())
                    .timeRange(getTimeSlotNameByUuid(candidate.getTimeSlotUuid()))
                    .suggestedSolutions(Arrays.asList("Buscar otro docente", "Cambiar horario"))
                    .build());
        }

        // Verificar conflicto de aula
        if (!isSpaceAvailable(candidate.getSpaceUuid(),
                candidate.getDayOfWeek(), getTimeSlotNameByUuid(candidate.getTimeSlotUuid()))) {
            detectedConflicts.add(ScheduleConflictDTO.builder()
                    .type("SPACE_CONFLICT")
                    .severity("CRITICAL")
                    .description("El aula ya está ocupada en este horario")
                    .affectedSpace(getSpaceNameByUuid(candidate.getSpaceUuid()))
                    .dayOfWeek(candidate.getDayOfWeek())
                    .timeRange(getTimeSlotNameByUuid(candidate.getTimeSlotUuid()))
                    .suggestedSolutions(Arrays.asList("Buscar otra aula", "Cambiar horario"))
                    .build());
        }

        return detectedConflicts;
    }

    // ===================== MÉTODOS AUXILIARES =====================

    private Set<UUID> convertHourRangesToUuids(List<String> hourRanges) {
        // Implementación simplificada - en producción sería más robusta
        return hourRanges.stream()
                .map(range -> UUID.randomUUID()) // Placeholder
                .collect(Collectors.toSet());
    }

    private UUID getTeacherUuidByName(String teacherName) {
        // Cache o servicio para resolver nombres a UUIDs
        // Por ahora retornamos null - implementar según necesidades
        return null;
    }

    private UUID getSpaceUuidByName(String spaceName) {
        // Cache o servicio para resolver nombres a UUIDs
        return null;
    }

    private UUID getGroupUuidByName(String groupName) {
        return groupRequirements.stream()
                .filter(group -> group.getGroupName().equals(groupName))
                .map(GroupRequirementDTO::getGroupUuid)
                .findFirst()
                .orElse(null);
    }

    private UUID getCourseUuidByName(String courseName) {
        return groupRequirements.stream()
                .flatMap(group -> group.getCourses().stream())
                .filter(course -> course.getCourseName().equals(courseName))
                .map(CourseRequirementDTO::getCourseUuid)
                .findFirst()
                .orElse(null);
    }

    private String getTimeSlotNameByUuid(UUID timeSlotUuid) {
        // Placeholder - implementar lookup real
        return "TimeSlot-" + timeSlotUuid.toString().substring(0, 8);
    }

    private String getTeacherNameByUuid(UUID teacherUuid) {
        // Placeholder - implementar lookup real
        return "Teacher-" + teacherUuid.toString().substring(0, 8);
    }

    private String getSpaceNameByUuid(UUID spaceUuid) {
        // Placeholder - implementar lookup real
        return "Space-" + spaceUuid.toString().substring(0, 8);
    }

    private LocalTime parseTimeFromRange(String timeRange, boolean getStart) {
        try {
            String[] parts = timeRange.split("-");
            String timeStr = getStart ? parts[0].trim() : parts[1].trim();
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            return LocalTime.MIDNIGHT;
        }
    }

    // ===================== MÉTODOS DE ESTADO =====================

    /**
     * Resetea el contexto para una nueva generación
     */
    public void reset() {
        conflicts.clear();
        warnings.clear();
        generatedSessions.clear();
        dailyHoursPerGroup.clear();
        occupiedSlots.clear();
        teacherAssignments.clear();
        spaceAssignments.clear();
    }

    /**
     * Obtiene un resumen del estado actual
     */
    public Map<String, Object> getStatusSummary() {
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalSessions", generatedSessions.size());
        summary.put("totalConflicts", conflicts.size());
        summary.put("totalWarnings", warnings.size());
        summary.put("groupsProcessed", groupRequirements.size());

        // Progreso por grupo
        Map<String, Double> groupProgress = new HashMap<>();
        for (GroupRequirementDTO group : groupRequirements) {
            double progress = getGroupProgress(group.getGroupUuid());
            groupProgress.put(group.getGroupName(), progress * 100); // Convertir a porcentaje
        }
        summary.put("groupProgress", groupProgress);

        return summary;
    }

    /**
     * Verifica si la generación está completa
     */
    public boolean isGenerationComplete() {
        return groupRequirements.stream()
                .allMatch(group -> getGroupProgress(group.getGroupUuid()) >= 0.95); // 95% completado
    }

    /**
     * Obtiene el grupo con menor progreso
     */
    public Optional<GroupRequirementDTO> getGroupWithLeastProgress() {
        return groupRequirements.stream()
                .min(Comparator.comparing(group -> getGroupProgress(group.getGroupUuid())));
    }

    /**
     * Calcula la puntuación general de calidad de la generación
     */
    public double calculateQualityScore() {
        double score = 100.0;

        // Penalización por conflictos
        score -= conflicts.size() * 15.0;

        // Penalización por advertencias
        score -= warnings.size() * 5.0;

        // Bonus por distribución equilibrada
        long balancedGroups = groupRequirements.stream()
                .filter(group -> !hasDistributionImbalance(group.getGroupUuid()))
                .count();

        if (groupRequirements.size() > 0) {
            double balanceRatio = (double) balancedGroups / groupRequirements.size();
            score += balanceRatio * 10.0;
        }

        // Bonus por completitud
        double avgProgress = groupRequirements.stream()
                .mapToDouble(group -> getGroupProgress(group.getGroupUuid()))
                .average().orElse(0.0);
        score += avgProgress * 20.0;

        return Math.max(0, Math.min(100, score));
    }





    /**
     * Inicializa el contexto con sesiones existentes
     */
    public void initializeWithExistingSessions(Map<UUID, List<ClassSessionEntity>> existingSessionsMap) {
        this.existingSessions.putAll(existingSessionsMap);

        // Extraer asignaciones docente-curso existentes
        for (Map.Entry<UUID, List<ClassSessionEntity>> entry : existingSessionsMap.entrySet()) {
            UUID groupUuid = entry.getKey();

            for (ClassSessionEntity session : entry.getValue()) {
                String courseTeacherKey = groupUuid + "_" + session.getCourse().getUuid();
                courseTeacherAssignments.put(courseTeacherKey, session.getTeacher().getUuid());

                // Marcar slots como preservados si vamos a mantener sesiones existentes
                for (TeachingHourEntity hour : session.getTeachingHours()) {
                    String slotKey = session.getDayOfWeek().name() + "_" +
                            hour.getTimeSlot().getName() + "_" + hour.getUuid();
                    preservedSlots.add(slotKey);
                }
            }
        }

        log.info("📊 Contexto inicializado con {} asignaciones docente-curso existentes",
                courseTeacherAssignments.size());
    }

    /**
     * Obtiene el docente ya asignado para un curso en un grupo específico
     */
    public UUID getAssignedTeacherForCourse(UUID groupUuid, UUID courseUuid) {
        String key = groupUuid + "_" + courseUuid;
        return courseTeacherAssignments.get(key);
    }

    /**
     * Registra una nueva asignación docente-curso
     */
    public void assignTeacherToCourse(UUID groupUuid, UUID courseUuid, UUID teacherUuid) {
        String key = groupUuid + "_" + courseUuid;
        courseTeacherAssignments.put(key, teacherUuid);
    }

    /**
     * Verifica si un slot debe ser preservado (tiene sesiones existentes que se mantienen)
     */
    public boolean isSlotPreserved(DayOfWeek dayOfWeek, String timeSlotName, UUID teachingHourUuid) {
        String slotKey = dayOfWeek.name() + "_" + timeSlotName + "_" + teachingHourUuid;
        return preservedSlots.contains(slotKey);
    }

    /**
     * Obtiene las sesiones existentes para un grupo
     */
    public List<ClassSessionEntity> getExistingSessionsForGroup(UUID groupUuid) {
        return existingSessions.getOrDefault(groupUuid, new ArrayList<>());
    }

    /**
     * Calcula qué cursos faltan por asignar para un grupo
     */
    public List<CourseRequirementDTO> getMissingCoursesForGroup(UUID groupUuid) {
        GroupRequirementDTO group = groupRequirements.stream()
                .filter(g -> g.getGroupUuid().equals(groupUuid))
                .findFirst()
                .orElse(null);

        if (group == null) return new ArrayList<>();

        List<ClassSessionEntity> existing = getExistingSessionsForGroup(groupUuid);
        Set<UUID> assignedCourseUuids = existing.stream()
                .map(session -> session.getCourse().getUuid())
                .collect(Collectors.toSet());

        // Filtrar cursos que ya tienen sesiones asignadas
        return group.getCourses().stream()
                .filter(course -> !assignedCourseUuids.contains(course.getCourseUuid()))
                .collect(Collectors.toList());
    }

    /**
     * Calcula las horas restantes para un curso específico
     */
    public int getRemainingHoursForCourse(UUID groupUuid, UUID courseUuid, String sessionType) {
        // Obtener requerimiento total del curso
        CourseRequirementDTO courseReq = groupRequirements.stream()
                .flatMap(group -> group.getCourses().stream())
                .filter(course -> course.getCourseUuid().equals(courseUuid))
                .findFirst()
                .orElse(null);

        if (courseReq == null) return 0;

        int totalRequired = "THEORY".equals(sessionType) ?
                courseReq.getTotalTheoryHours() : courseReq.getTotalPracticeHours();

        // Calcular horas ya asignadas
        List<ClassSessionEntity> existing = getExistingSessionsForGroup(groupUuid);
        int assignedHours = existing.stream()
                .filter(session -> session.getCourse().getUuid().equals(courseUuid))
                .filter(session -> session.getSessionType().getName().name().equals(sessionType))
                .mapToInt(session -> session.getTeachingHours().size())
                .sum();

        return Math.max(0, totalRequired - assignedHours);
    }

    /**
     * Verifica si un grupo necesita más asignaciones
     */
    public boolean groupNeedsMoreAssignments(UUID groupUuid) {
        GroupRequirementDTO group = groupRequirements.stream()
                .filter(g -> g.getGroupUuid().equals(groupUuid))
                .findFirst()
                .orElse(null);

        if (group == null) return false;

        int totalRequired = group.getTotalWeeklyHours();
        int totalAssigned = getTotalAssignedHours(groupUuid);

        return totalAssigned < totalRequired;
    }

    /**
     * Calcula el puntaje de calidad considerando sesiones existentes
     */
    public double calculateQualityScoreWithExisting() {
        double baseScore = calculateQualityScore();

        // Bonus por preservar continuidad de docentes
        long preservedTeacherContinuity = courseTeacherAssignments.size();
        double continuityBonus = Math.min(20.0, preservedTeacherContinuity * 2.0);

        // Bonus por aprovechamiento de sesiones existentes
        long totalExistingSessions = existingSessions.values().stream()
                .mapToLong(List::size)
                .sum();

        double reuseBonus = Math.min(15.0, totalExistingSessions * 1.0);

        return Math.min(100.0, baseScore + continuityBonus + reuseBonus);
    }

    /**
     * Genera reporte de integración con sesiones existentes
     */
    public Map<String, Object> getIntegrationReport() {
        Map<String, Object> report = new HashMap<>();

        // Estadísticas de preservación
        report.put("preservedTeacherAssignments", courseTeacherAssignments.size());
        report.put("preservedSlots", preservedSlots.size());
        report.put("groupsWithExistingSessions", existingSessions.size());

        // Análisis por grupo
        Map<String, Object> groupAnalysis = new HashMap<>();
        for (GroupRequirementDTO group : groupRequirements) {
            Map<String, Object> groupInfo = new HashMap<>();

            List<ClassSessionEntity> existing = getExistingSessionsForGroup(group.getGroupUuid());
            groupInfo.put("existingSessions", existing.size());
            groupInfo.put("needsMoreAssignments", groupNeedsMoreAssignments(group.getGroupUuid()));
            groupInfo.put("missingCourses", getMissingCoursesForGroup(group.getGroupUuid()).size());

            // Progreso por tipo de sesión
            Map<String, Integer> progressByType = new HashMap<>();
            for (CourseRequirementDTO course : group.getCourses()) {
                int theoryRemaining = getRemainingHoursForCourse(group.getGroupUuid(),
                        course.getCourseUuid(), "THEORY");
                int practiceRemaining = getRemainingHoursForCourse(group.getGroupUuid(),
                        course.getCourseUuid(), "PRACTICE");

                progressByType.put("theoryHoursRemaining",
                        progressByType.getOrDefault("theoryHoursRemaining", 0) + theoryRemaining);
                progressByType.put("practiceHoursRemaining",
                        progressByType.getOrDefault("practiceHoursRemaining", 0) + practiceRemaining);
            }
            groupInfo.put("remainingHoursByType", progressByType);

            groupAnalysis.put(group.getGroupName(), groupInfo);
        }
        report.put("groupAnalysis", groupAnalysis);

        // Métricas de eficiencia
        double integrationEfficiency = calculateIntegrationEfficiency();
        report.put("integrationEfficiency", integrationEfficiency);

        return report;
    }

    /**
     * Calcula la eficiencia de integración con sesiones existentes
     */
    private double calculateIntegrationEfficiency() {
        if (groupRequirements.isEmpty()) return 1.0;

        int totalGroups = groupRequirements.size();
        int groupsWithExisting = existingSessions.size();
        int preservedAssignments = courseTeacherAssignments.size();

        // Factores de eficiencia
        double reuseRatio = totalGroups > 0 ? (double) groupsWithExisting / totalGroups : 0.0;
        double continuityRatio = preservedAssignments > 0 ? 1.0 : 0.0;

        // Calcular eficiencia total
        return (reuseRatio * 0.6) + (continuityRatio * 0.4);
    }

    /**
     * Optimiza la estrategia de asignación basada en el contexto existente
     */
    public Map<String, Object> optimizeAssignmentStrategy() {
        Map<String, Object> strategy = new HashMap<>();

        // Análisis de distribución actual
        Map<DayOfWeek, Integer> currentDistribution = new HashMap<>();
        for (List<ClassSessionEntity> sessions : existingSessions.values()) {
            for (ClassSessionEntity session : sessions) {
                currentDistribution.merge(session.getDayOfWeek(),
                        session.getTeachingHours().size(), Integer::sum);
            }
        }

        // Identificar días con menor carga para priorizar
        DayOfWeek lightestDay = currentDistribution.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DayOfWeek.MONDAY);

        strategy.put("prioritizeDay", lightestDay);
        strategy.put("currentDistribution", currentDistribution);

        // Docentes con menor carga actual
        Map<UUID, Integer> teacherLoad = new HashMap<>();
        for (List<ClassSessionEntity> sessions : existingSessions.values()) {
            for (ClassSessionEntity session : sessions) {
                teacherLoad.merge(session.getTeacher().getUuid(),
                        session.getTeachingHours().size(), Integer::sum);
            }
        }

        List<UUID> availableTeachers = teacherLoad.entrySet().stream()
                .filter(entry -> entry.getValue() < 15) // Menos de 15 horas asignadas
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        strategy.put("availableTeachers", availableTeachers);

        // Recomendaciones estratégicas
        List<String> recommendations = new ArrayList<>();
        if (currentDistribution.isEmpty()) {
            recommendations.add("No hay restricciones por sesiones existentes");
        } else {
            recommendations.add("Priorizar " + lightestDay + " para nuevas asignaciones");
            recommendations.add("Mantener continuidad de " + courseTeacherAssignments.size() + " asignaciones docente-curso");
        }

        strategy.put("recommendations", recommendations);

        return strategy;
    }

    /**
     * Resetea solo los datos de tracking dinámico manteniendo configuración base
     */
    public void resetDynamicTracking() {
        dailyHoursPerGroup.clear();
        occupiedSlots.clear();
        teacherAssignments.clear();
        spaceAssignments.clear();

        // Mantener courseTeacherAssignments y preservedSlots para continuidad
        // Solo limpiar el tracking de sesiones generadas en esta ejecución
        generatedSessions.clear();
        conflicts.clear();
        warnings.clear();
    }

    /**
     * Verifica compatibilidad entre nueva asignación y sesiones existentes
     */
    public boolean isCompatibleWithExistingSessions(UUID groupUuid, UUID courseUuid,
                                                    UUID teacherUuid, DayOfWeek dayOfWeek, UUID timeSlotUuid) {

        List<ClassSessionEntity> existingForGroup = getExistingSessionsForGroup(groupUuid);

        // Verificar continuidad de docente para el curso
        UUID assignedTeacher = getAssignedTeacherForCourse(groupUuid, courseUuid);
        if (assignedTeacher != null && !assignedTeacher.equals(teacherUuid)) {
            return false; // Debe mantener el mismo docente
        }

        // Verificar que no hay conflicto temporal con sesiones existentes del grupo
        for (ClassSessionEntity existing : existingForGroup) {
            if (existing.getDayOfWeek().equals(dayOfWeek)) {
                String existingTimeSlot = existing.getTeachingHours().stream()
                        .findFirst()
                        .map(th -> th.getTimeSlot().getUuid())
                        .map(UUID::toString)
                        .orElse("");

                if (existingTimeSlot.equals(timeSlotUuid.toString())) {
                    return false; // Conflicto directo de horario
                }
            }
        }

        return true;
    }

    /**
     * Calcula bonus de puntuación por integración inteligente con sesiones existentes
     */
    public double calculateIntegrationBonus(UUID groupUuid, UUID courseUuid,
                                            UUID teacherUuid, DayOfWeek dayOfWeek, UUID timeSlotUuid) {

        double bonus = 0.0;

        // Bonus por mantener continuidad de docente
        UUID assignedTeacher = getAssignedTeacherForCourse(groupUuid, courseUuid);
        if (assignedTeacher != null && assignedTeacher.equals(teacherUuid)) {
            bonus += 30.0; // Alto bonus por continuidad
        }

        // Bonus por distribución equilibrada con sesiones existentes
        List<ClassSessionEntity> existingForGroup = getExistingSessionsForGroup(groupUuid);
        Map<DayOfWeek, Long> existingDistribution = existingForGroup.stream()
                .collect(Collectors.groupingBy(ClassSessionEntity::getDayOfWeek, Collectors.counting()));

        long currentDayCount = existingDistribution.getOrDefault(dayOfWeek, 0L);
        double avgDistribution = existingDistribution.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        if (currentDayCount < avgDistribution) {
            bonus += 15.0; // Bonus por equilibrar distribución
        }

        // Bonus por proximidad temporal (turnos adyacentes)
        for (ClassSessionEntity existing : existingForGroup) {
            if (existing.getDayOfWeek().equals(dayOfWeek)) {
                String existingTimeSlotName = existing.getTeachingHours().stream()
                        .findFirst()
                        .map(th -> th.getTimeSlot().getName())
                        .orElse("");

                // Este método ya existe en el servicio
                // Si son turnos adyacentes, dar bonus por continuidad
                if (isAdjacentTimeSlot(existingTimeSlotName, timeSlotUuid.toString())) {
                    bonus += 20.0;
                }
            }
        }

        return bonus;
    }

    /**
     * Obtiene estadísticas detalladas de la sesión actual
     */
    public Map<String, Object> getDetailedSessionStats() {
        Map<String, Object> stats = getStatusSummary();

        // Agregar información específica de integración
        stats.put("integrationReport", getIntegrationReport());
        stats.put("qualityScoreWithExisting", calculateQualityScoreWithExisting());

        // Estadísticas de preservación
        Map<String, Object> preservation = new HashMap<>();
        preservation.put("teacherContinuityMaintained", courseTeacherAssignments.size());
        preservation.put("slotsPreserved", preservedSlots.size());
        preservation.put("existingSessionsConsidered", existingSessions.values().stream()
                .mapToLong(List::size).sum());

        stats.put("preservationStats", preservation);

        // Análisis de eficiencia
        Map<String, Object> efficiency = new HashMap<>();
        efficiency.put("integrationEfficiency", calculateIntegrationEfficiency());
        efficiency.put("reuseRatio", existingSessions.size() > 0 ?
                (double) existingSessions.size() / groupRequirements.size() : 0.0);

        stats.put("efficiencyMetrics", efficiency);

        return stats;
    }

    // Método auxiliar para verificar turnos adyacentes (simplificado para el contexto)
    private boolean isAdjacentTimeSlot(String timeSlot1, String timeSlotUuid) {
        // Simplificación: extraer el nombre del turno desde el UUID si es necesario
        // En un caso real, se consultaría el servicio de TimeSlot

        Map<String, List<String>> adjacentSlots = Map.of(
                "M1", Arrays.asList("M2"),
                "M2", Arrays.asList("M1", "M3"),
                "M3", Arrays.asList("M2", "T1"),
                "T1", Arrays.asList("M3", "T2"),
                "T2", Arrays.asList("T1", "N1"),
                "N1", Arrays.asList("T2")
        );

        return adjacentSlots.getOrDefault(timeSlot1, new ArrayList<>())
                .stream()
                .anyMatch(adj -> timeSlotUuid.contains(adj));
    }
}
