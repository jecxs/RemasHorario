package com.pontificia.remashorario.modules.automaticSchedule.mapper;

import com.pontificia.remashorario.modules.TimeSlot.TimeSlotEntity;
import com.pontificia.remashorario.modules.automaticSchedule.dto.*;
import com.pontificia.remashorario.modules.classSession.ClassSessionEntity;
import com.pontificia.remashorario.modules.classSession.dto.ClassSessionRequestDTO;
import com.pontificia.remashorario.modules.classSession.mapper.ClassSessionMapper;
import com.pontificia.remashorario.modules.course.CourseEntity;
import com.pontificia.remashorario.modules.course.mapper.CourseMapper;
import com.pontificia.remashorario.modules.learningSpace.LearningSpaceEntity;
import com.pontificia.remashorario.modules.learningSpace.mapper.LearningSpaceMapper;
import com.pontificia.remashorario.modules.studentGroup.StudentGroupEntity;
import com.pontificia.remashorario.modules.studentGroup.mapper.StudentGroupMapper;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teacher.mapper.TeacherMapper;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScheduleGenerationMapper {

    private final ClassSessionMapper classSessionMapper;
    private final TeacherMapper teacherMapper;
    private final LearningSpaceMapper learningSpaceMapper;
    private final CourseMapper courseMapper;
    private final StudentGroupMapper studentGroupMapper;

    /**
     * Convierte una ClassSessionEntity a GeneratedClassSessionDTO
     */
    public GeneratedClassSessionDTO toGeneratedSessionDTO(ClassSessionEntity entity) {
        if (entity == null) return null;

        // Obtener rangos de horas pedagógicas
        List<String> hourRanges = entity.getTeachingHours().stream()
                .sorted(Comparator.comparing(TeachingHourEntity::getOrderInTimeSlot))
                .map(hour -> hour.getStartTime().toString().substring(0, 5) + "-" +
                        hour.getEndTime().toString().substring(0, 5))
                .collect(Collectors.toList());

        return GeneratedClassSessionDTO.builder()
                .uuid(entity.getUuid())
                .courseName(entity.getCourse().getName())
                .groupName(entity.getStudentGroup().getName())
                .teacherName(entity.getTeacher().getFullName())
                .learningSpaceName(entity.getLearningSpace().getName())
                .dayOfWeek(entity.getDayOfWeek())
                .timeSlotName(entity.getTeachingHours().stream()
                        .findFirst()
                        .map(th -> th.getTimeSlot().getName())
                        .orElse("N/A"))
                .teachingHourRanges(hourRanges)
                .sessionType(entity.getSessionType().getName().name())
                .notes(entity.getNotes())
                .isNewlyGenerated(false) // Por defecto, las existentes no son nuevas
                .build();
    }

    /**
     * Convierte una lista de ClassSessionEntity a GeneratedClassSessionDTO
     */
    public List<GeneratedClassSessionDTO> toGeneratedSessionDTOList(List<ClassSessionEntity> entities) {
        return entities.stream()
                .map(this::toGeneratedSessionDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un CourseEntity a CourseRequirementDTO
     */
    public CourseRequirementDTO toCourseRequirementDTO(CourseEntity entity) {
        if (entity == null) return null;

        return CourseRequirementDTO.builder()
                .courseUuid(entity.getUuid())
                .courseName(entity.getName())
                .cycleUuid(entity.getCycle().getUuid())
                .totalTheoryHours(entity.getWeeklyTheoryHours())
                .totalPracticeHours(entity.getWeeklyPracticeHours())
                .totalHours(entity.getWeeklyTheoryHours() + entity.getWeeklyPracticeHours())
                .requiredKnowledgeAreaUuid(entity.getTeachingKnowledgeArea().getUuid())
                .preferredSpecialtyUuid(entity.getPreferredSpecialty() != null ?
                        entity.getPreferredSpecialty().getUuid() : null)
                .supportedTeachingTypeUuids(entity.getTeachingTypes().stream()
                        .map(BaseEntity::getUuid)
                        .collect(Collectors.toList()))
                .isMixed(entity.getWeeklyTheoryHours() > 0 && entity.getWeeklyPracticeHours() > 0)
                .build();
    }

    /**
     * Convierte un StudentGroupEntity a GroupRequirementDTO
     */
    public GroupRequirementDTO toGroupRequirementDTO(StudentGroupEntity groupEntity, List<CourseEntity> courses) {
        if (groupEntity == null) return null;

        List<CourseRequirementDTO> courseRequirements = courses.stream()
                .map(this::toCourseRequirementDTO)
                .collect(Collectors.toList());

        int totalWeeklyHours = courseRequirements.stream()
                .mapToInt(CourseRequirementDTO::getTotalHours)
                .sum();

        return GroupRequirementDTO.builder()
                .groupUuid(groupEntity.getUuid())
                .groupName(groupEntity.getName())
                .cycleUuid(groupEntity.getCycle().getUuid())
                .periodUuid(groupEntity.getPeriod().getUuid())
                .courses(courseRequirements)
                .totalWeeklyHours(totalWeeklyHours)
                .build();
    }

    /**
     * Convierte TeacherEntity y disponibilidad a TeacherAvailabilitySlotDTO
     */
    public TeacherAvailabilitySlotDTO toTeacherAvailabilitySlotDTO(
            TeacherEntity teacher,
            DayOfWeek dayOfWeek,
            UUID timeSlotUuid,
            String timeSlotName,
            List<UUID> availableHourUuids,
            boolean isPreferred) {

        return TeacherAvailabilitySlotDTO.builder()
                .teacherUuid(teacher.getUuid())
                .teacherName(teacher.getFullName())
                .dayOfWeek(dayOfWeek)
                .timeSlotUuid(timeSlotUuid)
                .timeSlotName(timeSlotName)
                .availableTeachingHourUuids(availableHourUuids)
                .knowledgeAreaUuids(teacher.getKnowledgeAreas().stream()
                        .map(BaseEntity::getUuid)
                        .collect(Collectors.toList()))
                .isPreferred(isPreferred)
                .build();
    }

    /**
     * Convierte LearningSpaceEntity a SpaceAvailabilitySlotDTO
     */
    public SpaceAvailabilitySlotDTO toSpaceAvailabilitySlotDTO(
            LearningSpaceEntity space,
            DayOfWeek dayOfWeek,
            UUID timeSlotUuid,
            List<UUID> availableHourUuids,
            boolean isPreferred) {

        return SpaceAvailabilitySlotDTO.builder()
                .spaceUuid(space.getUuid())
                .spaceName(space.getName())
                .spaceType(space.getTypeUUID().getName().name())
                .specialtyUuid(space.getSpecialty() != null ? space.getSpecialty().getUuid() : null)
                .specialtyName(space.getSpecialty() != null ? space.getSpecialty().getName() : null)
                .capacity(space.getCapacity())
                .dayOfWeek(dayOfWeek)
                .timeSlotUuid(timeSlotUuid)
                .availableTeachingHourUuids(availableHourUuids)
                .isPreferred(isPreferred)
                .build();
    }

    /**
     * Convierte TimeSlotEntity a ScheduleSlotDTO
     */
    public ScheduleSlotDTO toScheduleSlotDTO(TimeSlotEntity timeSlot, DayOfWeek dayOfWeek, boolean isPreferred) {
        if (timeSlot == null) return null;

        List<UUID> teachingHourUuids = timeSlot.getTeachingHours().stream()
                .sorted(Comparator.comparing(TeachingHourEntity::getOrderInTimeSlot))
                .map(BaseEntity::getUuid)
                .collect(Collectors.toList());

        return ScheduleSlotDTO.builder()
                .dayOfWeek(dayOfWeek)
                .timeSlotUuid(timeSlot.getUuid())
                .timeSlotName(timeSlot.getName())
                .teachingHourUuids(teachingHourUuids)
                .startTime(timeSlot.getStartTime())
                .endTime(timeSlot.getEndTime())
                .durationMinutes((int) Duration.between(timeSlot.getStartTime(), timeSlot.getEndTime()).toMinutes())
                .isPreferred(isPreferred)
                .build();
    }

    /**
     * Convierte AssignmentCandidateDTO a ClassSessionRequestDTO para crear sesión real
     */
    public ClassSessionRequestDTO toClassSessionRequestDTO(AssignmentCandidateDTO candidate, UUID sessionTypeUuid, UUID periodUuid) {
        if (candidate == null) return null;

        ClassSessionRequestDTO request = new ClassSessionRequestDTO();
        request.setStudentGroupUuid(candidate.getGroupUuid());
        request.setCourseUuid(candidate.getCourseUuid());
        request.setTeacherUuid(candidate.getTeacherUuid());
        request.setLearningSpaceUuid(candidate.getSpaceUuid());
        request.setDayOfWeek(candidate.getDayOfWeek());
        request.setSessionTypeUuid(sessionTypeUuid);
        request.setTeachingHourUuids(candidate.getTeachingHourUuids());
        request.setNotes("Generado automáticamente");

        return request;
    }

    /**
     * Crea un ScheduleConflictDTO a partir de información de conflicto
     */
    public ScheduleConflictDTO createConflictDTO(
            String type,
            String severity,
            String description,
            String affectedElement,
            DayOfWeek dayOfWeek,
            String timeRange,
            List<String> solutions) {

        ScheduleConflictDTO.ScheduleConflictDTOBuilder builder = ScheduleConflictDTO.builder()
                .type(type)
                .severity(severity)
                .description(description)
                .dayOfWeek(dayOfWeek)
                .timeRange(timeRange)
                .suggestedSolutions(solutions != null ? solutions : new ArrayList<>());

        // Asignar el elemento afectado según el tipo
        switch (type) {
            case "TEACHER_CONFLICT":
                builder.affectedTeacher(affectedElement);
                break;
            case "SPACE_CONFLICT":
                builder.affectedSpace(affectedElement);
                break;
            case "GROUP_CONFLICT":
                builder.affectedGroup(affectedElement);
                break;
            case "COURSE_CONFLICT":
                builder.affectedCourse(affectedElement);
                break;
        }

        return builder.build();
    }

    /**
     * Crea un ScheduleWarningDTO
     */
    public ScheduleWarningDTO createWarningDTO(
            String type,
            String message,
            String affectedGroup,
            String suggestion,
            DayOfWeek dayOfWeek) {

        return ScheduleWarningDTO.builder()
                .type(type)
                .message(message)
                .affectedGroup(affectedGroup)
                .suggestion(suggestion)
                .dayOfWeek(dayOfWeek)
                .build();
    }

    /**
     * Convierte estadísticas de sesiones a ScheduleStatisticsDTO
     */
    public ScheduleStatisticsDTO toScheduleStatisticsDTO(
            List<GeneratedClassSessionDTO> sessions,
            Map<UUID, Integer> teacherHours,
            Map<UUID, Integer> spaceHours) {

        // Sesiones por día
        Map<String, Integer> sessionsPerDay = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getDayOfWeek().name(),
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

        // Sesiones por turno
        Map<String, Integer> sessionsPerTimeSlot = sessions.stream()
                .collect(Collectors.groupingBy(
                        GeneratedClassSessionDTO::getTimeSlotName,
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

        // Horas por curso
        Map<String, Integer> hoursPerCourse = sessions.stream()
                .collect(Collectors.groupingBy(
                        GeneratedClassSessionDTO::getCourseName,
                        Collectors.summingInt(s -> s.getTeachingHourRanges().size())
                ));

        // Utilización de docentes (simplificada)
        Map<String, Double> teacherUtilization = new HashMap<>();
        if (teacherHours != null) {
            teacherHours.forEach((uuid, hours) -> {
                double utilization = Math.min(1.0, hours / 20.0); // Máximo 20 horas por semana
                teacherUtilization.put(uuid.toString(), utilization);
            });
        }

        // Utilización de espacios (simplificada)
        Map<String, Double> spaceUtilization = new HashMap<>();
        if (spaceHours != null) {
            spaceHours.forEach((uuid, hours) -> {
                double utilization = Math.min(1.0, hours / 30.0); // Máximo 30 horas por semana
                spaceUtilization.put(uuid.toString(), utilization);
            });
        }

        double averageHoursPerDay = sessions.isEmpty() ? 0 :
                sessions.stream().mapToInt(s -> s.getTeachingHourRanges().size()).sum() / 6.0;

        double distributionBalance = calculateDistributionBalance(sessionsPerDay);

        return ScheduleStatisticsDTO.builder()
                .sessionsPerDay(sessionsPerDay)
                .sessionsPerTimeSlot(sessionsPerTimeSlot)
                .teacherUtilization(teacherUtilization)
                .spaceUtilization(spaceUtilization)
                .hoursPerCourse(hoursPerCourse)
                .averageHoursPerDay(averageHoursPerDay)
                .distributionBalance(distributionBalance)
                .build();
    }

    /**
     * Calcula el balance de distribución basado en sesiones por día
     */
    private double calculateDistributionBalance(Map<String, Integer> sessionsPerDay) {
        if (sessionsPerDay.isEmpty()) return 1.0;

        double average = sessionsPerDay.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        if (average == 0) return 1.0;

        double variance = sessionsPerDay.values().stream()
                .mapToDouble(count -> Math.pow(count - average, 2))
                .average()
                .orElse(0.0);

        // Convertir varianza a puntuación de balance (0-1)
        double normalizedVariance = variance / (average * average);
        return Math.max(0, 1 - normalizedVariance);
    }

    /**
     * Convierte información de restricciones a ScheduleConstraintsDTO
     */
    public ScheduleConstraintsDTO toScheduleConstraintsDTO(
            int totalGroups,
            int totalCourses,
            int totalRequiredHours,
            int availableTeachers,
            int availableSpaces,
            int availableTimeSlots) {

        List<String> constraints = new ArrayList<>();

        // Analizar restricciones potenciales
        double hourSlotRatio = availableTimeSlots > 0 ?
                (double) totalRequiredHours / (availableTimeSlots * 6) : 0; // 6 días por semana

        if (hourSlotRatio > 0.8) {
            constraints.add("Alta densidad de horas requeridas vs. slots disponibles");
        }

        double teacherCourseRatio = availableTeachers > 0 ?
                (double) totalCourses / availableTeachers : 0;

        if (teacherCourseRatio > 3) {
            constraints.add("Posible sobrecarga de docentes especializados");
        }

        if (totalGroups > availableSpaces) {
            constraints.add("Más grupos que aulas disponibles - posibles conflictos de espacio");
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

    /**
     * Crea ScheduleFeasibilityDTO basado en análisis de factibilidad
     */
    public ScheduleFeasibilityDTO toScheduleFeasibilityDTO(
            ScheduleConstraintsDTO constraints,
            List<GroupRequirementDTO> requirements) {

        List<String> challenges = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        // Calcular puntuación de factibilidad
        double feasibilityScore = 1.0;

        // Factor 1: Ratio horas/slots
        double hourSlotRatio = constraints.getAvailableTimeSlots() > 0 ?
                (double) constraints.getTotalRequiredHours() / (constraints.getAvailableTimeSlots() * 6 * 3) : 0;

        if (hourSlotRatio > 0.9) {
            feasibilityScore -= 0.4;
            challenges.add("Capacidad de horarios casi al límite");
            recommendations.add("Considere incrementar turnos o redistribuir horas");
        } else if (hourSlotRatio > 0.7) {
            feasibilityScore -= 0.2;
            challenges.add("Alta utilización de capacidad horaria");
            recommendations.add("Monitoree la distribución cuidadosamente");
        }

        // Factor 2: Disponibilidad de docentes
        double teacherRatio = constraints.getAvailableTeachers() > 0 ?
                (double) constraints.getTotalCourses() / constraints.getAvailableTeachers() : 0;

        if (teacherRatio > 4) {
            feasibilityScore -= 0.3;
            challenges.add("Alta carga promedio por docente");
            recommendations.add("Verifique competencias específicas de docentes");
        }

        // Factor 3: Complejidad de grupos
        if (constraints.getTotalGroups() > 15) {
            feasibilityScore -= 0.1;
            challenges.add("Coordinación compleja con muchos grupos");
            recommendations.add("Considere generación por fases");
        }

        // Factor 4: Cursos complejos (mixtos)
        long mixedCourses = requirements.stream()
                .flatMap(group -> group.getCourses().stream())
                .filter(CourseRequirementDTO::getIsMixed)
                .count();

        if (mixedCourses > constraints.getTotalCourses() * 0.6) {
            feasibilityScore -= 0.1;
            challenges.add("Alto porcentaje de cursos mixtos (teoría + práctica)");
            recommendations.add("Priorice laboratorios después de teoría");
        }

        // Recomendaciones generales
        if (feasibilityScore > 0.8) {
            recommendations.add("Excelentes condiciones para generación automática");
        } else if (feasibilityScore > 0.6) {
            recommendations.add("Condiciones aceptables - proceda con configuración óptima");
        } else {
            recommendations.add("Condiciones desafiantes - considere ajustar parámetros");
        }

        boolean isFeasible = feasibilityScore >= 0.5 && challenges.size() <= 3;

        return ScheduleFeasibilityDTO.builder()
                .isFeasible(isFeasible)
                .feasibilityScore(Math.max(0, Math.min(1, feasibilityScore)))
                .challenges(challenges)
                .recommendations(recommendations)
                .build();
    }

    /**
     * Convierte lista de conflictos del contexto a ScheduleConflictDTO
     */
    public List<ScheduleConflictDTO> mapContextConflicts(List<String> contextConflicts) {
        return contextConflicts.stream()
                .map(conflict -> ScheduleConflictDTO.builder()
                        .type("GENERAL_CONFLICT")
                        .severity("MEDIUM")
                        .description(conflict)
                        .suggestedSolutions(Arrays.asList("Revisar configuración", "Ajustar parámetros"))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Convierte resultado de validación a formato de respuesta
     */
    public Map<String, Object> toValidationResponse(
            boolean isValid,
            List<String> errors,
            List<String> warnings,
            List<String> recommendations) {

        Map<String, Object> response = new HashMap<>();
        response.put("isValid", isValid);
        response.put("errors", errors != null ? errors : new ArrayList<>());
        response.put("warnings", warnings != null ? warnings : new ArrayList<>());
        response.put("recommendations", recommendations != null ? recommendations : new ArrayList<>());

        // Calcular puntuación de calidad de configuración
        double configQuality = 1.0;
        if (errors != null) configQuality -= errors.size() * 0.3;
        if (warnings != null) configQuality -= warnings.size() * 0.1;
        configQuality = Math.max(0, Math.min(1, configQuality));

        response.put("configurationQuality", configQuality);

        return response;
    }

    /**
     * Mapea estadísticas de utilización de recursos
     */
    public Map<String, Object> toResourceUtilizationMap(
            Map<UUID, Set<String>> teacherAssignments,
            Map<UUID, Set<String>> spaceAssignments,
            int totalTimeSlots) {

        Map<String, Object> utilization = new HashMap<>();

        // Utilización de docentes
        Map<String, Double> teacherUtil = new HashMap<>();
        teacherAssignments.forEach((teacherUuid, assignments) -> {
            double util = (double) assignments.size() / totalTimeSlots;
            teacherUtil.put(teacherUuid.toString(), Math.min(1.0, util));
        });
        utilization.put("teachers", teacherUtil);

        // Utilización de espacios
        Map<String, Double> spaceUtil = new HashMap<>();
        spaceAssignments.forEach((spaceUuid, assignments) -> {
            double util = (double) assignments.size() / totalTimeSlots;
            spaceUtil.put(spaceUuid.toString(), Math.min(1.0, util));
        });
        utilization.put("spaces", spaceUtil);

        // Estadísticas generales
        double avgTeacherUtil = teacherUtil.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double avgSpaceUtil = spaceUtil.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        utilization.put("averageTeacherUtilization", avgTeacherUtil);
        utilization.put("averageSpaceUtilization", avgSpaceUtil);
        utilization.put("overallEfficiency", (avgTeacherUtil + avgSpaceUtil) / 2.0);

        return utilization;
    }

    /**
     * Crea resumen de progreso por grupo
     */
    public Map<String, Object> toProgressSummary(
            List<GroupRequirementDTO> groups,
            Map<UUID, Integer> assignedHours) {

        Map<String, Object> summary = new HashMap<>();
        Map<String, Double> groupProgress = new HashMap<>();

        double totalProgress = 0.0;
        int completedGroups = 0;

        for (GroupRequirementDTO group : groups) {
            int assigned = assignedHours.getOrDefault(group.getGroupUuid(), 0);
            double progress = group.getTotalWeeklyHours() > 0 ?
                    (double) assigned / group.getTotalWeeklyHours() : 0.0;

            groupProgress.put(group.getGroupName(), progress * 100); // Porcentaje
            totalProgress += progress;

            if (progress >= 0.95) completedGroups++;
        }

        summary.put("groupProgress", groupProgress);
        summary.put("averageProgress", groups.isEmpty() ? 0.0 : (totalProgress / groups.size()) * 100);
        summary.put("completedGroups", completedGroups);
        summary.put("totalGroups", groups.size());
        summary.put("completionRate", groups.isEmpty() ? 0.0 : (double) completedGroups / groups.size());

        return summary;
    }

    /**
     * Convierte tiempo de ejecución y métricas a resumen de rendimiento
     */
    public Map<String, Object> toPerformanceSummary(
            long executionTimeMs,
            int totalSessions,
            int totalGroups,
            int totalCourses) {

        Map<String, Object> performance = new HashMap<>();
        performance.put("executionTimeMs", executionTimeMs);
        performance.put("executionTimeSeconds", executionTimeMs / 1000.0);

        // Métricas de rendimiento
        if (executionTimeMs > 0) {
            performance.put("sessionsPerSecond", totalSessions / (executionTimeMs / 1000.0));
            performance.put("groupsPerSecond", totalGroups / (executionTimeMs / 1000.0));
            performance.put("coursesPerSecond", totalCourses / (executionTimeMs / 1000.0));
        }

        // Clasificación de rendimiento
        String performanceRating;
        if (executionTimeMs < 5000) performanceRating = "EXCELLENT";
        else if (executionTimeMs < 15000) performanceRating = "GOOD";
        else if (executionTimeMs < 30000) performanceRating = "ACCEPTABLE";
        else performanceRating = "SLOW";

        performance.put("performanceRating", performanceRating);

        return performance;
    }
}
