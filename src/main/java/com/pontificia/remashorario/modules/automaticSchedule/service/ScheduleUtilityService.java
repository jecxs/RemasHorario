package com.pontificia.remashorario.modules.automaticSchedule.service;

import com.pontificia.remashorario.modules.TimeSlot.TimeSlotService;
import com.pontificia.remashorario.modules.TimeSlot.dto.TimeSlotResponseDTO;
import com.pontificia.remashorario.modules.automaticSchedule.dto.ScheduleConflictDTO;
import com.pontificia.remashorario.modules.automaticSchedule.dto.ScheduleGenerationRequestDTO;
import com.pontificia.remashorario.modules.automaticSchedule.mapper.ScheduleGenerationMapper;
import com.pontificia.remashorario.modules.classSession.ClassSessionEntity;
import com.pontificia.remashorario.modules.classSession.ClassSessionRepository;
import com.pontificia.remashorario.modules.course.CourseEntity;
import com.pontificia.remashorario.modules.course.CourseService;
import com.pontificia.remashorario.modules.learningSpace.LearningSpaceService;
import com.pontificia.remashorario.modules.learningSpace.dto.LearningSpaceResponseDTO;
import com.pontificia.remashorario.modules.studentGroup.StudentGroupEntity;
import com.pontificia.remashorario.modules.studentGroup.StudentGroupService;
import com.pontificia.remashorario.modules.studentGroup.dto.StudentGroupResponseDTO;
import com.pontificia.remashorario.modules.teacher.TeacherService;
import com.pontificia.remashorario.modules.teacher.dto.TeacherResponseDTO;
import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherWithAvailabilitiesDTO;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleUtilityService {

    private final ClassSessionRepository classSessionRepository;
    private final StudentGroupService studentGroupService;
    private final TeacherService teacherService;
    private final LearningSpaceService learningSpaceService;
    private final TimeSlotService timeSlotService;
    private final ScheduleGenerationMapper scheduleMapper;
    private final CourseService courseService;

    /**
     * Limpia todas las sesiones de clase de un periodo específico
     */
    @Transactional
    public Map<String, Object> clearPeriodSchedules(UUID periodUuid, UUID careerUuid, UUID cycleUuid) {
        log.info("🧹 Iniciando limpieza de horarios para periodo: {}", periodUuid);

        List<ClassSessionEntity> sessionsToDelete = new ArrayList<>();

        if (cycleUuid != null) {
            // Limpiar solo un ciclo específico
            sessionsToDelete = classSessionRepository.findByCycleUuid(cycleUuid).stream()
                    .filter(session -> session.getPeriod().getUuid().equals(periodUuid))
                    .collect(Collectors.toList());
        } else if (careerUuid != null) {
            // Limpiar toda una carrera
            sessionsToDelete = classSessionRepository.findByCareerUuid(careerUuid).stream()
                    .filter(session -> session.getPeriod().getUuid().equals(periodUuid))
                    .collect(Collectors.toList());
        } else {
            // Limpiar todo el periodo
            sessionsToDelete = classSessionRepository.findByPeriod(periodUuid);
        }

        // Obtener estadísticas antes de eliminar
        Set<UUID> affectedGroups = sessionsToDelete.stream()
                .map(session -> session.getStudentGroup().getUuid())
                .collect(Collectors.toSet());

        Set<UUID> affectedTeachers = sessionsToDelete.stream()
                .map(session -> session.getTeacher().getUuid())
                .collect(Collectors.toSet());

        Set<UUID> affectedSpaces = sessionsToDelete.stream()
                .map(session -> session.getLearningSpace().getUuid())
                .collect(Collectors.toSet());

        // Eliminar sesiones
        classSessionRepository.deleteAll(sessionsToDelete);

        Map<String, Object> result = new HashMap<>();
        result.put("deletedSessions", sessionsToDelete.size());
        result.put("affectedGroups", affectedGroups.size());
        result.put("affectedTeachers", affectedTeachers.size());
        result.put("affectedSpaces", affectedSpaces.size());
        result.put("periodUuid", periodUuid);

        log.info("✅ Limpieza completada: {} sesiones eliminadas", sessionsToDelete.size());

        return result;
    }

    /**
     * Analiza la ocupación actual del sistema
     */
    public Map<String, Object> analyzeSystemOccupancy(UUID periodUuid) {
        log.info("📊 Analizando ocupación del sistema para periodo: {}", periodUuid);

        List<ClassSessionEntity> allSessions = classSessionRepository.findByPeriod(periodUuid);

        Map<String, Object> analysis = new HashMap<>();

        // Estadísticas generales
        analysis.put("totalSessions", allSessions.size());
        analysis.put("totalHours", allSessions.stream()
                .mapToInt(session -> session.getTeachingHours().size())
                .sum());

        // Ocupación por día
        Map<String, Long> sessionsByDay = allSessions.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getDayOfWeek().name(),
                        Collectors.counting()
                ));
        analysis.put("sessionsByDay", sessionsByDay);

        // Ocupación por turno
        Map<String, Long> sessionsByTimeSlot = allSessions.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getTeachingHours().stream()
                                .findFirst()
                                .map(th -> th.getTimeSlot().getName())
                                .orElse("UNKNOWN"),
                        Collectors.counting()
                ));
        analysis.put("sessionsByTimeSlot", sessionsByTimeSlot);

        // Top docentes más ocupados
        Map<String, Long> teacherOccupancy = allSessions.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getTeacher().getFullName(),
                        Collectors.counting()
                ));

        List<Map.Entry<String, Long>> topTeachers = teacherOccupancy.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
        analysis.put("topBusyTeachers", topTeachers);

        // Top aulas más utilizadas
        Map<String, Long> spaceOccupancy = allSessions.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getLearningSpace().getName(),
                        Collectors.counting()
                ));

        List<Map.Entry<String, Long>> topSpaces = spaceOccupancy.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
        analysis.put("topUsedSpaces", topSpaces);

        // Análisis de distribución temporal
        Map<String, Object> timeDistribution = analyzeTimeDistribution(allSessions);
        analysis.put("timeDistribution", timeDistribution);

        return analysis;
    }

    /**
     * Detecta conflictos en horarios existentes
     */
    public Map<String, Object> detectExistingConflicts(UUID periodUuid) {
        log.info("🔍 Detectando conflictos en horarios existentes");

        List<ClassSessionEntity> allSessions = classSessionRepository.findByPeriod(periodUuid);
        List<ScheduleConflictDTO> conflicts = new ArrayList<>();

        // Agrupar sesiones por día
        Map<DayOfWeek, List<ClassSessionEntity>> sessionsByDay = allSessions.stream()
                .collect(Collectors.groupingBy(ClassSessionEntity::getDayOfWeek));

        for (Map.Entry<DayOfWeek, List<ClassSessionEntity>> dayEntry : sessionsByDay.entrySet()) {
            DayOfWeek day = dayEntry.getKey();
            List<ClassSessionEntity> daySessions = dayEntry.getValue();

            // Detectar conflictos de docente
            conflicts.addAll(detectTeacherConflicts(daySessions, day));

            // Detectar conflictos de aula
            conflicts.addAll(detectSpaceConflicts(daySessions, day));

            // Detectar conflictos de grupo
            conflicts.addAll(detectGroupConflicts(daySessions, day));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalConflicts", conflicts.size());
        result.put("conflicts", conflicts);
        result.put("conflictsByType", conflicts.stream()
                .collect(Collectors.groupingBy(ScheduleConflictDTO::getType, Collectors.counting())));
        result.put("conflictsBySeverity", conflicts.stream()
                .collect(Collectors.groupingBy(ScheduleConflictDTO::getSeverity, Collectors.counting())));

        return result;
    }

    /**
     * Genera reporte de utilización de recursos
     */
    public Map<String, Object> generateUtilizationReport(UUID periodUuid) {
        log.info("📈 Generando reporte de utilización para periodo: {}", periodUuid);

        List<ClassSessionEntity> allSessions = classSessionRepository.findByPeriod(periodUuid);

        Map<String, Object> report = new HashMap<>();

        // Utilización de docentes
        Map<String, Object> teacherUtilization = calculateTeacherUtilization(allSessions);
        report.put("teacherUtilization", teacherUtilization);

        // Utilización de aulas
        Map<String, Object> spaceUtilization = calculateSpaceUtilization(allSessions);
        report.put("spaceUtilization", spaceUtilization);

        // Utilización temporal
        Map<String, Object> timeUtilization = calculateTimeUtilization(allSessions);
        report.put("timeUtilization", timeUtilization);

        // Métricas de calidad
        Map<String, Object> qualityMetrics = calculateQualityMetrics(allSessions);
        report.put("qualityMetrics", qualityMetrics);

        return report;
    }

    /**
     * Sugiere optimizaciones para horarios existentes
     */
    public Map<String, Object> suggestOptimizations(UUID periodUuid) {
        log.info("💡 Analizando optimizaciones posibles");

        List<ClassSessionEntity> allSessions = classSessionRepository.findByPeriod(periodUuid);
        List<String> suggestions = new ArrayList<>();

        // Analizar gaps de tiempo
        Map<UUID, List<String>> groupGaps = analyzeGroupTimeGaps(allSessions);
        if (!groupGaps.isEmpty()) {
            suggestions.add("Se detectaron gaps de tiempo en " + groupGaps.size() + " grupos");
            suggestions.add("Considere reorganizar sesiones para eliminar tiempos muertos");
        }

        // Analizar distribución desbalanceada
        Map<UUID, Map<DayOfWeek, Integer>> groupDistribution = analyzeGroupDistribution(allSessions);
        long unbalancedGroups = groupDistribution.entrySet().stream()
                .filter(entry -> isUnbalancedDistribution(entry.getValue()))
                .count();

        if (unbalancedGroups > 0) {
            suggestions.add("Se detectaron " + unbalancedGroups + " grupos con distribución desbalanceada");
            suggestions.add("Redistribuya sesiones para equilibrar la carga semanal");
        }

        // Analizar subutilización de recursos
        Map<String, Double> resourceUtilization = calculateResourceUtilizationScores(allSessions);
        if (resourceUtilization.get("teachers") < 0.6) {
            suggestions.add("Baja utilización de docentes (" +
                    String.format("%.1f%%", resourceUtilization.get("teachers") * 100) + ")");
        }
        if (resourceUtilization.get("spaces") < 0.6) {
            suggestions.add("Baja utilización de aulas (" +
                    String.format("%.1f%%", resourceUtilization.get("spaces") * 100) + ")");
        }

        // Sugerencias específicas
        if (suggestions.isEmpty()) {
            suggestions.add("Los horarios actuales tienen una buena optimización");
            suggestions.add("No se detectaron mejoras significativas");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("optimizationScore", calculateOptimizationScore(allSessions));
        result.put("suggestions", suggestions);
        result.put("detailedAnalysis", Map.of(
                "groupGaps", groupGaps.size(),
                "unbalancedGroups", unbalancedGroups,
                "resourceUtilization", resourceUtilization
        ));

        return result;
    }

    // ===================== MÉTODOS AUXILIARES =====================

    private Map<String, Object> analyzeTimeDistribution(List<ClassSessionEntity> sessions) {
        Map<String, Object> distribution = new HashMap<>();

        // Distribución por hora del día
        Map<Integer, Long> hourDistribution = sessions.stream()
                .flatMap(session -> session.getTeachingHours().stream())
                .collect(Collectors.groupingBy(
                        th -> th.getStartTime().getHour(),
                        Collectors.counting()
                ));
        distribution.put("hourDistribution", hourDistribution);

        // Picos de ocupación
        int peakHour = hourDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(8);
        distribution.put("peakHour", peakHour);

        // Horas con menor ocupación
        int lowHour = hourDistribution.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(17);
        distribution.put("lowOccupancyHour", lowHour);

        return distribution;
    }

    private List<ScheduleConflictDTO> detectTeacherConflicts(List<ClassSessionEntity> sessions, DayOfWeek day) {
        List<ScheduleConflictDTO> conflicts = new ArrayList<>();

        Map<UUID, List<ClassSessionEntity>> sessionsByTeacher = sessions.stream()
                .collect(Collectors.groupingBy(s -> s.getTeacher().getUuid()));

        for (Map.Entry<UUID, List<ClassSessionEntity>> entry : sessionsByTeacher.entrySet()) {
            List<ClassSessionEntity> teacherSessions = entry.getValue();
            if (teacherSessions.size() <= 1) continue;

            // Verificar superposición de horarios
            for (int i = 0; i < teacherSessions.size(); i++) {
                for (int j = i + 1; j < teacherSessions.size(); j++) {
                    if (hasTimeOverlap(teacherSessions.get(i), teacherSessions.get(j))) {
                        conflicts.add(scheduleMapper.createConflictDTO(
                                "TEACHER_CONFLICT",
                                "CRITICAL",
                                "Docente con sesiones superpuestas",
                                teacherSessions.get(i).getTeacher().getFullName(),
                                day,
                                getSessionTimeRange(teacherSessions.get(i)),
                                Arrays.asList("Reasignar docente", "Cambiar horario")
                        ));
                    }
                }
            }
        }

        return conflicts;
    }

    private List<ScheduleConflictDTO> detectSpaceConflicts(List<ClassSessionEntity> sessions, DayOfWeek day) {
        List<ScheduleConflictDTO> conflicts = new ArrayList<>();

        Map<UUID, List<ClassSessionEntity>> sessionsBySpace = sessions.stream()
                .collect(Collectors.groupingBy(s -> s.getLearningSpace().getUuid()));

        for (Map.Entry<UUID, List<ClassSessionEntity>> entry : sessionsBySpace.entrySet()) {
            List<ClassSessionEntity> spaceSessions = entry.getValue();
            if (spaceSessions.size() <= 1) continue;

            for (int i = 0; i < spaceSessions.size(); i++) {
                for (int j = i + 1; j < spaceSessions.size(); j++) {
                    if (hasTimeOverlap(spaceSessions.get(i), spaceSessions.get(j))) {
                        conflicts.add(scheduleMapper.createConflictDTO(
                                "SPACE_CONFLICT",
                                "CRITICAL",
                                "Aula con sesiones superpuestas",
                                spaceSessions.get(i).getLearningSpace().getName(),
                                day,
                                getSessionTimeRange(spaceSessions.get(i)),
                                Arrays.asList("Reasignar aula", "Cambiar horario")
                        ));
                    }
                }
            }
        }

        return conflicts;
    }

    private List<ScheduleConflictDTO> detectGroupConflicts(List<ClassSessionEntity> sessions, DayOfWeek day) {
        List<ScheduleConflictDTO> conflicts = new ArrayList<>();

        Map<UUID, List<ClassSessionEntity>> sessionsByGroup = sessions.stream()
                .collect(Collectors.groupingBy(s -> s.getStudentGroup().getUuid()));

        for (Map.Entry<UUID, List<ClassSessionEntity>> entry : sessionsByGroup.entrySet()) {
            List<ClassSessionEntity> groupSessions = entry.getValue();
            if (groupSessions.size() <= 1) continue;

            for (int i = 0; i < groupSessions.size(); i++) {
                for (int j = i + 1; j < groupSessions.size(); j++) {
                    if (hasTimeOverlap(groupSessions.get(i), groupSessions.get(j))) {
                        conflicts.add(scheduleMapper.createConflictDTO(
                                "GROUP_CONFLICT",
                                "CRITICAL",
                                "Grupo con sesiones superpuestas",
                                groupSessions.get(i).getStudentGroup().getName(),
                                day,
                                getSessionTimeRange(groupSessions.get(i)),
                                Arrays.asList("Reorganizar horario del grupo")
                        ));
                    }
                }
            }
        }

        return conflicts;
    }

    private boolean hasTimeOverlap(ClassSessionEntity session1, ClassSessionEntity session2) {
        Set<TeachingHourEntity> hours1 = session1.getTeachingHours();
        Set<TeachingHourEntity> hours2 = session2.getTeachingHours();

        for (TeachingHourEntity hour1 : hours1) {
            for (TeachingHourEntity hour2 : hours2) {
                if (hour1.getStartTime().isBefore(hour2.getEndTime()) &&
                        hour2.getStartTime().isBefore(hour1.getEndTime())) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getSessionTimeRange(ClassSessionEntity session) {
        LocalTime startTime = session.getTeachingHours().stream()
                .map(TeachingHourEntity::getStartTime)
                .min(LocalTime::compareTo)
                .orElse(LocalTime.MIDNIGHT);

        LocalTime endTime = session.getTeachingHours().stream()
                .map(TeachingHourEntity::getEndTime)
                .max(LocalTime::compareTo)
                .orElse(LocalTime.MIDNIGHT);

        return startTime.toString().substring(0, 5) + " - " + endTime.toString().substring(0, 5);
    }

    private Map<String, Object> calculateTeacherUtilization(List<ClassSessionEntity> sessions) {
        Map<String, Object> utilization = new HashMap<>();

        // Horas por docente
        Map<String, Integer> hoursPerTeacher = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getTeacher().getFullName(),
                        Collectors.summingInt(s -> s.getTeachingHours().size())
                ));

        // Estadísticas
        int totalTeachers = hoursPerTeacher.size();
        double averageHours = hoursPerTeacher.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        int maxHours = hoursPerTeacher.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);

        int minHours = hoursPerTeacher.values().stream()
                .mapToInt(Integer::intValue)
                .min()
                .orElse(0);

        utilization.put("totalTeachers", totalTeachers);
        utilization.put("averageHoursPerTeacher", averageHours);
        utilization.put("maxHoursPerTeacher", maxHours);
        utilization.put("minHoursPerTeacher", minHours);
        utilization.put("hoursDistribution", hoursPerTeacher);

        // Docentes sobrecargados (más de 20 horas)
        long overloadedTeachers = hoursPerTeacher.values().stream()
                .filter(hours -> hours > 20)
                .count();
        utilization.put("overloadedTeachers", overloadedTeachers);

        // Docentes subutilizados (menos de 8 horas)
        long underutilizedTeachers = hoursPerTeacher.values().stream()
                .filter(hours -> hours < 8)
                .count();
        utilization.put("underutilizedTeachers", underutilizedTeachers);

        return utilization;
    }

    private Map<String, Object> calculateSpaceUtilization(List<ClassSessionEntity> sessions) {
        Map<String, Object> utilization = new HashMap<>();

        // Horas por aula
        Map<String, Integer> hoursPerSpace = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getLearningSpace().getName(),
                        Collectors.summingInt(s -> s.getTeachingHours().size())
                ));

        // Estadísticas
        int totalSpaces = hoursPerSpace.size();
        double averageHours = hoursPerSpace.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        utilization.put("totalSpaces", totalSpaces);
        utilization.put("averageHoursPerSpace", averageHours);
        utilization.put("hoursDistribution", hoursPerSpace);

        // Aulas más y menos utilizadas
        String mostUsedSpace = hoursPerSpace.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        String leastUsedSpace = hoursPerSpace.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        utilization.put("mostUsedSpace", mostUsedSpace);
        utilization.put("leastUsedSpace", leastUsedSpace);

        return utilization;
    }

    private Map<String, Object> calculateTimeUtilization(List<ClassSessionEntity> sessions) {
        Map<String, Object> utilization = new HashMap<>();

        // Utilización por turno
        Map<String, Long> sessionsByTimeSlot = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getTeachingHours().stream()
                                .findFirst()
                                .map(th -> th.getTimeSlot().getName())
                                .orElse("UNKNOWN"),
                        Collectors.counting()
                ));

        utilization.put("sessionsByTimeSlot", sessionsByTimeSlot);

        // Utilización por día
        Map<String, Long> sessionsByDay = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getDayOfWeek().name(),
                        Collectors.counting()
                ));

        utilization.put("sessionsByDay", sessionsByDay);

        // Día más ocupado
        String busiestDay = sessionsByDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        utilization.put("busiestDay", busiestDay);

        return utilization;
    }

    private Map<String, Object> calculateQualityMetrics(List<ClassSessionEntity> sessions) {
        Map<String, Object> metrics = new HashMap<>();

        // Grupos con gaps de tiempo
        Map<UUID, List<String>> groupGaps = analyzeGroupTimeGaps(sessions);
        metrics.put("groupsWithTimeGaps", groupGaps.size());

        // Distribución equilibrada
        Map<UUID, Map<DayOfWeek, Integer>> groupDistribution = analyzeGroupDistribution(sessions);
        long wellDistributedGroups = groupDistribution.entrySet().stream()
                .filter(entry -> !isUnbalancedDistribution(entry.getValue()))
                .count();

        metrics.put("wellDistributedGroups", wellDistributedGroups);
        metrics.put("totalGroups", groupDistribution.size());
        metrics.put("distributionScore", groupDistribution.isEmpty() ? 0.0 :
                (double) wellDistributedGroups / groupDistribution.size());

        // Continuidad de docentes
        double teacherContinuityScore = calculateTeacherContinuityScore(sessions);
        metrics.put("teacherContinuityScore", teacherContinuityScore);

        // Puntuación general de calidad
        double overallQuality = (
                (double) wellDistributedGroups / Math.max(1, groupDistribution.size()) * 0.4 +
                        teacherContinuityScore * 0.3 +
                        (1.0 - (double) groupGaps.size() / Math.max(1, groupDistribution.size())) * 0.3
        );

        metrics.put("overallQualityScore", overallQuality);

        return metrics;
    }

    private Map<UUID, List<String>> analyzeGroupTimeGaps(List<ClassSessionEntity> sessions) {
        Map<UUID, List<String>> groupGaps = new HashMap<>();

        Map<UUID, Map<DayOfWeek, List<ClassSessionEntity>>> sessionsByGroupAndDay = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getStudentGroup().getUuid(),
                        Collectors.groupingBy(ClassSessionEntity::getDayOfWeek)
                ));

        for (Map.Entry<UUID, Map<DayOfWeek, List<ClassSessionEntity>>> groupEntry : sessionsByGroupAndDay.entrySet()) {
            UUID groupUuid = groupEntry.getKey();
            List<String> gaps = new ArrayList<>();

            for (Map.Entry<DayOfWeek, List<ClassSessionEntity>> dayEntry : groupEntry.getValue().entrySet()) {
                List<ClassSessionEntity> daySessions = dayEntry.getValue();
                if (daySessions.size() < 2) continue;

                // Ordenar sesiones por hora de inicio
                daySessions.sort((s1, s2) -> {
                    LocalTime start1 = s1.getTeachingHours().stream()
                            .map(TeachingHourEntity::getStartTime)
                            .min(LocalTime::compareTo)
                            .orElse(LocalTime.MAX);
                    LocalTime start2 = s2.getTeachingHours().stream()
                            .map(TeachingHourEntity::getStartTime)
                            .min(LocalTime::compareTo)
                            .orElse(LocalTime.MAX);
                    return start1.compareTo(start2);
                });

                // Detectar gaps
                for (int i = 0; i < daySessions.size() - 1; i++) {
                    LocalTime endTime = daySessions.get(i).getTeachingHours().stream()
                            .map(TeachingHourEntity::getEndTime)
                            .max(LocalTime::compareTo)
                            .orElse(LocalTime.MIN);

                    LocalTime nextStartTime = daySessions.get(i + 1).getTeachingHours().stream()
                            .map(TeachingHourEntity::getStartTime)
                            .min(LocalTime::compareTo)
                            .orElse(LocalTime.MAX);

                    long gapMinutes = Duration.between(endTime, nextStartTime).toMinutes();
                    if (gapMinutes > 45) { // Gap significativo
                        gaps.add(dayEntry.getKey().name() + ": " + endTime + " - " + nextStartTime +
                                " (" + gapMinutes + " min)");
                    }
                }
            }

            if (!gaps.isEmpty()) {
                groupGaps.put(groupUuid, gaps);
            }
        }

        return groupGaps;
    }

    private Map<UUID, Map<DayOfWeek, Integer>> analyzeGroupDistribution(List<ClassSessionEntity> sessions) {
        return sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getStudentGroup().getUuid(),
                        Collectors.groupingBy(
                                ClassSessionEntity::getDayOfWeek,
                                Collectors.summingInt(s -> s.getTeachingHours().size())
                        )
                ));
    }

    private boolean isUnbalancedDistribution(Map<DayOfWeek, Integer> dailyHours) {
        if (dailyHours.size() < 2) return false;

        double average = dailyHours.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        double variance = dailyHours.values().stream()
                .mapToDouble(hours -> Math.pow(hours - average, 2))
                .average()
                .orElse(0.0);

        double standardDeviation = Math.sqrt(variance);

        // Considera desbalanceado si la desviación estándar > 25% del promedio
        return standardDeviation > average * 0.25;
    }

    private double calculateTeacherContinuityScore(List<ClassSessionEntity> sessions) {
        Map<String, Set<UUID>> courseTeachers = new HashMap<>();

        for (ClassSessionEntity session : sessions) {
            String courseGroupKey = session.getCourse().getUuid() + "_" + session.getStudentGroup().getUuid();
            courseTeachers.computeIfAbsent(courseGroupKey, k -> new HashSet<>())
                    .add(session.getTeacher().getUuid());
        }

        // Calcular porcentaje de cursos con un solo docente
        long coursesWithSingleTeacher = courseTeachers.values().stream()
                .filter(teachers -> teachers.size() == 1)
                .count();

        return courseTeachers.isEmpty() ? 1.0 :
                (double) coursesWithSingleTeacher / courseTeachers.size();
    }

    private Map<String, Double> calculateResourceUtilizationScores(List<ClassSessionEntity> sessions) {
        Map<String, Double> scores = new HashMap<>();

        // Puntuación de utilización de docentes
        Map<UUID, Integer> teacherHours = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getTeacher().getUuid(),
                        Collectors.summingInt(s -> s.getTeachingHours().size())
                ));

        double avgTeacherHours = teacherHours.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        scores.put("teachers", Math.min(1.0, avgTeacherHours / 15.0)); // 15 horas como objetivo

        // Puntuación de utilización de aulas
        Map<UUID, Integer> spaceHours = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getLearningSpace().getUuid(),
                        Collectors.summingInt(s -> s.getTeachingHours().size())
                ));

        double avgSpaceHours = spaceHours.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        scores.put("spaces", Math.min(1.0, avgSpaceHours / 25.0)); // 25 horas como objetivo

        return scores;
    }

    private double calculateOptimizationScore(List<ClassSessionEntity> sessions) {
        double score = 100.0;

        // Penalización por gaps de tiempo
        Map<UUID, List<String>> gaps = analyzeGroupTimeGaps(sessions);
        score -= gaps.size() * 5.0;

        // Penalización por distribución desbalanceada
        Map<UUID, Map<DayOfWeek, Integer>> distribution = analyzeGroupDistribution(sessions);
        long unbalanced = distribution.entrySet().stream()
                .filter(entry -> isUnbalancedDistribution(entry.getValue()))
                .count();
        score -= unbalanced * 10.0;

        // Bonus por continuidad de docentes
        double continuity = calculateTeacherContinuityScore(sessions);
        score += continuity * 15.0;

        // Bonus por utilización equilibrada
        Map<String, Double> utilization = calculateResourceUtilizationScores(sessions);
        double avgUtilization = utilization.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        score += avgUtilization * 10.0;

        return Math.max(0, Math.min(100, score));
    }

    /**
     * Exporta horarios a formato CSV
     */
    public String exportToCSV(List<ClassSessionEntity> sessions) {
        StringBuilder csv = new StringBuilder();

        // Encabezados
        csv.append("Grupo,Curso,Docente,Aula,Día,Turno,Horas,Tipo,Notas\n");

        // Datos
        for (ClassSessionEntity session : sessions) {
            String hourRanges = session.getTeachingHours().stream()
                    .sorted(Comparator.comparing(TeachingHourEntity::getOrderInTimeSlot))
                    .map(hour -> hour.getStartTime().toString().substring(0, 5) + "-" +
                            hour.getEndTime().toString().substring(0, 5))
                    .collect(Collectors.joining(";"));

            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    escapeCsvField(session.getStudentGroup().getName()),
                    escapeCsvField(session.getCourse().getName()),
                    escapeCsvField(session.getTeacher().getFullName()),
                    escapeCsvField(session.getLearningSpace().getName()),
                    session.getDayOfWeek().name(),
                    session.getTeachingHours().stream()
                            .findFirst()
                            .map(th -> th.getTimeSlot().getName())
                            .orElse("N/A"),
                    hourRanges,
                    session.getSessionType().getName().name(),
                    escapeCsvField(session.getNotes() != null ? session.getNotes() : "")
            ));
        }

        return csv.toString();
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";

        // Escapar comillas y envolver en comillas si contiene comas
        String escaped = field.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    // Agregar estos métodos al ScheduleUtilityService existente

    /**
     * Detecta si grupos ya tienen horarios asignados y proporciona opciones
     */
    public Map<String, Object> analyzeExistingSchedules(UUID periodUuid, List<UUID> targetGroupUuids) {
        log.info("🔍 Analizando horarios existentes para {} grupos", targetGroupUuids.size());

        Map<String, Object> analysis = new HashMap<>();
        List<Map<String, Object>> groupStatus = new ArrayList<>();

        for (UUID groupUuid : targetGroupUuids) {
            List<ClassSessionEntity> existingSessions = classSessionRepository
                    .findByStudentGroupUuidAndPeriod(groupUuid, periodUuid);

            Map<String, Object> groupInfo = new HashMap<>();
            groupInfo.put("groupUuid", groupUuid);
            groupInfo.put("hasExistingSessions", !existingSessions.isEmpty());
            groupInfo.put("sessionCount", existingSessions.size());

            if (!existingSessions.isEmpty()) {
                // Calcular estadísticas de las sesiones existentes
                int totalHours = existingSessions.stream()
                        .mapToInt(session -> session.getTeachingHours().size())
                        .sum();

                Set<UUID> assignedCourses = existingSessions.stream()
                        .map(session -> session.getCourse().getUuid())
                        .collect(Collectors.toSet());

                Set<UUID> assignedTeachers = existingSessions.stream()
                        .map(session -> session.getTeacher().getUuid())
                        .collect(Collectors.toSet());

                Map<DayOfWeek, Long> sessionsByDay = existingSessions.stream()
                        .collect(Collectors.groupingBy(
                                ClassSessionEntity::getDayOfWeek,
                                Collectors.counting()
                        ));

                groupInfo.put("totalAssignedHours", totalHours);
                groupInfo.put("assignedCoursesCount", assignedCourses.size());
                groupInfo.put("assignedTeachersCount", assignedTeachers.size());
                groupInfo.put("distributionByDay", sessionsByDay);

                // Estimar porcentaje de completitud
                // Esto requeriría conocer los cursos del ciclo
                groupInfo.put("estimatedCompleteness", calculateGroupCompleteness(groupUuid, totalHours));
            }

            groupStatus.add(groupInfo);
        }

        // Estadísticas generales
        long groupsWithSessions = groupStatus.stream()
                .filter(group -> (Boolean) group.get("hasExistingSessions"))
                .count();

        analysis.put("totalGroups", targetGroupUuids.size());
        analysis.put("groupsWithExistingSessions", groupsWithSessions);
        analysis.put("groupsWithoutSessions", targetGroupUuids.size() - groupsWithSessions);
        analysis.put("groupDetails", groupStatus);

        // Recomendaciones
        List<String> recommendations = new ArrayList<>();
        if (groupsWithSessions > 0) {
            recommendations.add("Se detectaron " + groupsWithSessions + " grupos con horarios existentes");
            recommendations.add("Puede elegir entre: RESET (eliminar existentes) o COMPLETE (completar faltantes)");
        }
        if (groupsWithSessions < targetGroupUuids.size()) {
            recommendations.add((targetGroupUuids.size() - groupsWithSessions) + " grupos están listos para generación automática");
        }

        analysis.put("recommendations", recommendations);
        analysis.put("needsUserDecision", groupsWithSessions > 0);

        return analysis;
    }

    private double calculateGroupCompleteness(UUID groupUuid, int assignedHours) {
        try {
            // Obtener el grupo y sus cursos del ciclo
            StudentGroupEntity group = studentGroupService.findOrThrow(groupUuid);
            List<CourseEntity> cycleCourses = courseService.getCoursesByCycle(group.getCycle().getUuid());

            int totalRequiredHours = cycleCourses.stream()
                    .mapToInt(course -> course.getWeeklyTheoryHours() + course.getWeeklyPracticeHours())
                    .sum();

            return totalRequiredHours > 0 ? (double) assignedHours / totalRequiredHours : 0.0;
        } catch (Exception e) {
            log.warn("Error calculando completitud para grupo {}: {}", groupUuid, e.getMessage());
            return 0.0;
        }
    }

    /**
     * Analiza la carga de trabajo y recursos disponibles para optimización
     */
    public Map<String, Object> analyzeWorkloadAndResources(UUID periodUuid, List<UUID> targetGroupUuids) {
        log.info("📊 Analizando carga de trabajo y recursos disponibles");

        Map<String, Object> analysis = new HashMap<>();

        // Analizar carga de docentes
        Map<UUID, Integer> teacherCurrentLoad = new HashMap<>();
        List<ClassSessionEntity> allPeriodSessions = classSessionRepository.findByPeriod(periodUuid);

        for (ClassSessionEntity session : allPeriodSessions) {
            UUID teacherUuid = session.getTeacher().getUuid();
            int hours = session.getTeachingHours().size();
            teacherCurrentLoad.merge(teacherUuid, hours, Integer::sum);
        }

        // Analizar ocupación de aulas
        Map<UUID, Integer> spaceCurrentLoad = new HashMap<>();
        for (ClassSessionEntity session : allPeriodSessions) {
            UUID spaceUuid = session.getLearningSpace().getUuid();
            int hours = session.getTeachingHours().size();
            spaceCurrentLoad.merge(spaceUuid, hours, Integer::sum);
        }

        // Calcular métricas
        double avgTeacherLoad = teacherCurrentLoad.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        double avgSpaceLoad = spaceCurrentLoad.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        // Identificar recursos sobrecargados
        List<String> overloadedTeachers = teacherCurrentLoad.entrySet().stream()
                .filter(entry -> entry.getValue() > 20) // Más de 20 horas
                .map(entry -> entry.getKey().toString())
                .collect(Collectors.toList());

        List<String> overloadedSpaces = spaceCurrentLoad.entrySet().stream()
                .filter(entry -> entry.getValue() > 30) // Más de 30 horas
                .map(entry -> entry.getKey().toString())
                .collect(Collectors.toList());

        analysis.put("averageTeacherLoad", avgTeacherLoad);
        analysis.put("averageSpaceLoad", avgSpaceLoad);
        analysis.put("overloadedTeachers", overloadedTeachers);
        analysis.put("overloadedSpaces", overloadedSpaces);
        analysis.put("systemUtilization", (avgTeacherLoad + avgSpaceLoad) / 50.0); // Normalizado

        // Recomendaciones específicas
        List<String> recommendations = new ArrayList<>();
        if (avgTeacherLoad > 15) {
            recommendations.add("Alta carga promedio de docentes - considere distribución cuidadosa");
        }
        if (overloadedTeachers.size() > 0) {
            recommendations.add(overloadedTeachers.size() + " docentes sobrecargados detectados");
        }
        if (avgSpaceLoad > 25) {
            recommendations.add("Alta utilización de aulas - verifique disponibilidad");
        }

        analysis.put("recommendations", recommendations);

        return analysis;
    }

    /**
     * Limpia horarios con opción selectiva por grupos
     */
    @Transactional
    public Map<String, Object> selectiveClearSchedules(UUID periodUuid, List<UUID> groupUuids, boolean resetAll) {
        log.info("🧹 Limpieza selectiva - {} grupos, resetAll: {}", groupUuids.size(), resetAll);

        List<ClassSessionEntity> sessionsToDelete = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();

        if (resetAll) {
            // Eliminar todas las sesiones de los grupos especificados
            for (UUID groupUuid : groupUuids) {
                List<ClassSessionEntity> groupSessions = classSessionRepository
                        .findByStudentGroupUuidAndPeriod(groupUuid, periodUuid);
                sessionsToDelete.addAll(groupSessions);
            }
        } else {
            // Solo eliminar sesiones que podrían interferir con la nueva generación
            // Por ejemplo, sesiones incompletas o con conflictos
            for (UUID groupUuid : groupUuids) {
                List<ClassSessionEntity> groupSessions = classSessionRepository
                        .findByStudentGroupUuidAndPeriod(groupUuid, periodUuid);

                // Filtrar sesiones problemáticas (ejemplo: sesiones solitarias de un curso)
                Map<UUID, List<ClassSessionEntity>> sessionsByCourse = groupSessions.stream()
                        .collect(Collectors.groupingBy(s -> s.getCourse().getUuid()));

                for (Map.Entry<UUID, List<ClassSessionEntity>> entry : sessionsByCourse.entrySet()) {
                    List<ClassSessionEntity> courseSessions = entry.getValue();

                    // Si un curso tiene muy pocas sesiones, podría estar incompleto
                    int totalHours = courseSessions.stream()
                            .mapToInt(s -> s.getTeachingHours().size())
                            .sum();

                    if (totalHours < 3) { // Menos de 3 horas asignadas
                        sessionsToDelete.addAll(courseSessions);
                    }
                }
            }
        }

        // Obtener estadísticas antes de eliminar
        Set<UUID> affectedGroups = sessionsToDelete.stream()
                .map(s -> s.getStudentGroup().getUuid())
                .collect(Collectors.toSet());

        Set<UUID> affectedCourses = sessionsToDelete.stream()
                .map(s -> s.getCourse().getUuid())
                .collect(Collectors.toSet());

        // Eliminar sesiones
        classSessionRepository.deleteAll(sessionsToDelete);

        result.put("deletedSessions", sessionsToDelete.size());
        result.put("affectedGroups", affectedGroups.size());
        result.put("affectedCourses", affectedCourses.size());
        result.put("cleanupStrategy", resetAll ? "RESET_ALL" : "SELECTIVE");
        result.put("groupsProcessed", groupUuids.size());

        log.info("✅ Limpieza completada: {} sesiones eliminadas", sessionsToDelete.size());

        return result;
    }
}
