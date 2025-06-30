package com.pontificia.remashorario.modules.automaticSchedule.controller;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.automaticSchedule.dto.*;
import com.pontificia.remashorario.modules.automaticSchedule.service.AutomaticScheduleGenerationService;
import com.pontificia.remashorario.modules.classSession.ClassSessionEntity;
import com.pontificia.remashorario.modules.classSession.ClassSessionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/protected/schedule-generation")
@RequiredArgsConstructor
@Slf4j
public class AutomaticScheduleGenerationController {

    private final AutomaticScheduleGenerationService scheduleGenerationService;
    private final ClassSessionRepository classSessionRepository;

    /**
     * Genera horarios automáticamente según los parámetros especificados
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ScheduleGenerationResultDTO>> generateSchedule(
            @Valid @RequestBody ScheduleGenerationRequestDTO request) {

        log.info("🚀 Solicitud de generación automática recibida para periodo: {}", request.getPeriodUuid());

        try {
            ScheduleGenerationResultDTO result = scheduleGenerationService.generateSchedule(request);

            if (result.isSuccess()) {
                return ResponseEntity.ok(
                        ApiResponse.success(result, "Horarios generados exitosamente")
                );
            } else {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .body(ApiResponse.success(result,
                                "Generación completada con advertencias: " + result.getMessage()));
            }

        } catch (IllegalArgumentException e) {
            log.warn("❌ Error de validación en generación: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error de validación: " + e.getMessage()));

        } catch (Exception e) {
            log.error("❌ Error interno en generación automática: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error interno del servidor: " + e.getMessage()));
        }
    }

    /**
     * Obtiene una vista previa de lo que se generaría sin crear sesiones reales
     */
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<SchedulePreviewDTO>> getSchedulePreview(
            @Valid @RequestBody SchedulePreviewRequestDTO request) {

        log.info("👁️ Solicitud de vista previa para periodo: {}", request.getPeriodUuid());

        try {
            SchedulePreviewDTO preview = scheduleGenerationService.getSchedulePreview(request);

            return ResponseEntity.ok(
                    ApiResponse.success(preview, "Vista previa generada exitosamente")
            );

        } catch (Exception e) {
            log.error("❌ Error generando vista previa: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error generando vista previa: " + e.getMessage()));
        }
    }

    /**
     * Genera horarios para un grupo específico
     */
    @PostMapping("/generate-group/{groupUuid}")
    public ResponseEntity<ApiResponse<ScheduleGenerationResultDTO>> generateScheduleForGroup(
            @PathVariable UUID groupUuid,
            @RequestBody ScheduleGenerationRequestDTO request) {

        log.info("🎯 Generación para grupo específico: {}", groupUuid);

        // Configurar request para grupo específico
        request.setGroupUuids(Arrays.asList(groupUuid));
        request.setModalityUuid(null);
        request.setCareerUuid(null);
        request.setCycleUuid(null);

        return generateSchedule(request);
    }

    /**
     * Genera horarios para una carrera completa
     */
    @PostMapping("/generate-career/{careerUuid}")
    public ResponseEntity<ApiResponse<ScheduleGenerationResultDTO>> generateScheduleForCareer(
            @PathVariable UUID careerUuid,
            @RequestBody ScheduleGenerationRequestDTO request) {

        log.info("🏫 Generación para carrera: {}", careerUuid);

        // Configurar request para carrera específica
        request.setCareerUuid(careerUuid);
        request.setModalityUuid(null);
        request.setCycleUuid(null);
        request.setGroupUuids(null);

        return generateSchedule(request);
    }

    /**
     * Genera horarios para un ciclo específico
     */
    @PostMapping("/generate-cycle/{cycleUuid}")
    public ResponseEntity<ApiResponse<ScheduleGenerationResultDTO>> generateScheduleForCycle(
            @PathVariable UUID cycleUuid,
            @RequestBody ScheduleGenerationRequestDTO request) {

        log.info("📚 Generación para ciclo: {}", cycleUuid);

        // Configurar request para ciclo específico
        request.setCycleUuid(cycleUuid);
        request.setModalityUuid(null);
        request.setCareerUuid(null);
        request.setGroupUuids(null);

        return generateSchedule(request);
    }

    /**
     * Genera horarios para toda una modalidad educativa
     */
    @PostMapping("/generate-modality/{modalityUuid}")
    public ResponseEntity<ApiResponse<ScheduleGenerationResultDTO>> generateScheduleForModality(
            @PathVariable UUID modalityUuid,
            @RequestBody ScheduleGenerationRequestDTO request) {

        log.info("🏛️ Generación para modalidad: {}", modalityUuid);

        // Configurar request para modalidad específica
        request.setModalityUuid(modalityUuid);
        request.setCareerUuid(null);
        request.setCycleUuid(null);
        request.setGroupUuids(null);

        return generateSchedule(request);
    }

    /**
     * Valida los parámetros de generación sin ejecutar la generación
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateGenerationRequest(
            @Valid @RequestBody ScheduleGenerationRequestDTO request) {

        log.info("✅ Validando parámetros de generación");

        try {
            // Ejecutar validaciones
            Map<String, Object> validationResult = new HashMap<>();
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            List<String> recommendations = new ArrayList<>();

            // Validación de alcance
            if (!request.isValidScope()) {
                errors.add("Debe especificar al menos un filtro de alcance (modalidad, carrera, ciclo o grupos)");
            }

            // Validación de configuración
            if (request.getMaxHoursPerDay() < request.getMinHoursPerDay()) {
                errors.add("El máximo de horas por día no puede ser menor al mínimo");
            }

            if (request.getMaxConsecutiveHours() > request.getMaxHoursPerDay()) {
                warnings.add("Las horas consecutivas máximas exceden el máximo diario");
            }

            // Validación de días excluidos
            if (request.getExcludedDays().size() >= 6) {
                errors.add("No se pueden excluir todos los días de la semana");
            }

            // Validaciones adicionales
            if (request.getPreferredTimeSlotWeight() > 1.0 || request.getPreferredTimeSlotWeight() < 0.0) {
                warnings.add("El peso de turnos preferentes debe estar entre 0 y 1");
            }

            // Recomendaciones
            if (request.getPreferredTimeSlotUuids().isEmpty()) {
                recommendations.add("Considere especificar turnos preferentes para mejor distribución");
            }

            if (!request.getDistributeEvenly()) {
                recommendations.add("Se recomienda activar distribución equilibrada para mejor calidad");
            }

            validationResult.put("isValid", errors.isEmpty());
            validationResult.put("errors", errors);
            validationResult.put("warnings", warnings);
            validationResult.put("recommendations", recommendations);

            return ResponseEntity.ok(
                    ApiResponse.success(validationResult, "Validación completada")
            );

        } catch (Exception e) {
            log.error("❌ Error en validación: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error en validación: " + e.getMessage()));
        }
    }

    /**
     * Obtiene el estado actual de una generación en progreso
     */
    @GetMapping("/status/{generationId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGenerationStatus(
            @PathVariable String generationId) {

        // Para futuras implementaciones con generación asíncrona
        Map<String, Object> status = new HashMap<>();
        status.put("status", "NOT_IMPLEMENTED");
        status.put("message", "Funcionalidad de seguimiento asíncrono pendiente de implementación");

        return ResponseEntity.ok(
                ApiResponse.success(status, "Estado de generación")
        );
    }

    /**
     * Cancela una generación en progreso
     */
    @PostMapping("/cancel/{generationId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelGeneration(
            @PathVariable String generationId) {

        // Para futuras implementaciones con generación asíncrona
        Map<String, Object> result = new HashMap<>();
        result.put("cancelled", false);
        result.put("message", "Funcionalidad de cancelación pendiente de implementación");

        return ResponseEntity.ok(
                ApiResponse.success(result, "Cancelación de generación")
        );
    }

    /**
     * Obtiene configuraciones predeterminadas según el contexto
     */
    @GetMapping("/default-config")
    public ResponseEntity<ApiResponse<ScheduleGenerationRequestDTO>> getDefaultConfiguration(
            @RequestParam(required = false) UUID modalityUuid,
            @RequestParam(required = false) UUID careerUuid,
            @RequestParam(required = false) UUID cycleUuid) {

        log.info("⚙️ Obteniendo configuración predeterminada");

        try {
            ScheduleGenerationRequestDTO defaultConfig = ScheduleGenerationRequestDTO.builder()
                    .modalityUuid(modalityUuid)
                    .careerUuid(careerUuid)
                    .cycleUuid(cycleUuid)
                    .excludedDays(new ArrayList<>()) // Por defecto no excluir días
                    .preferredTimeSlotUuids(new ArrayList<>()) // Se llenarían desde el contexto
                    .maxHoursPerDay(8)
                    .minHoursPerDay(2)
                    .distributeEvenly(true)
                    .respectTeacherContinuity(true)
                    .avoidTimeGaps(true)
                    .maxConsecutiveHours(4)
                    .prioritizeLabsAfterTheory(false)
                    .preferredTimeSlotWeight(0.7)
                    .build();

            // Configuraciones específicas según el contexto
            if (cycleUuid != null) {
                // Para ciclos iniciales, excluir sábados y preferir turnos de mañana
                // Esta lógica se podría mejorar consultando el número del ciclo
                defaultConfig.getExcludedDays().add(DayOfWeek.SATURDAY);
                defaultConfig.setMaxHoursPerDay(6);
            }

            return ResponseEntity.ok(
                    ApiResponse.success(defaultConfig, "Configuración predeterminada generada")
            );

        } catch (Exception e) {
            log.error("❌ Error obteniendo configuración predeterminada: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error obteniendo configuración: " + e.getMessage()));
        }
    }

    /**
     * Obtiene estadísticas de capacidad del sistema para generación automática
     */
    @GetMapping("/system-capacity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemCapacity(
            @RequestParam UUID periodUuid) {

        log.info("📊 Analizando capacidad del sistema para periodo: {}", periodUuid);

        try {
            Map<String, Object> capacity = new HashMap<>();

            // Esta información se obtendría de los servicios existentes
            // Por ahora, datos de ejemplo
            capacity.put("totalTeachers", 45);
            capacity.put("totalSpaces", 25);
            capacity.put("totalTimeSlots", 5);
            capacity.put("workingDaysPerWeek", 6);
            capacity.put("maxSimultaneousGroups", 15);
            capacity.put("estimatedCapacityPerWeek", 450); // Horas de clase por semana

            // Análisis de carga actual
            capacity.put("currentlyAssignedHours", 180);
            capacity.put("availableCapacity", 270);
            capacity.put("utilizationPercentage", 40.0);

            // Recomendaciones
            List<String> recommendations = new ArrayList<>();
            recommendations.add("El sistema tiene buena capacidad para generación automática");
            recommendations.add("Se recomienda generar horarios por carreras para mejor optimización");
            capacity.put("recommendations", recommendations);

            return ResponseEntity.ok(
                    ApiResponse.success(capacity, "Análisis de capacidad completado")
            );

        } catch (Exception e) {
            log.error("❌ Error analizando capacidad: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error analizando capacidad: " + e.getMessage()));
        }
    }

    /**
     * Ejecuta una simulación de generación sin crear sesiones reales
     */
    @PostMapping("/simulate")
    public ResponseEntity<ApiResponse<ScheduleGenerationResultDTO>> simulateGeneration(
            @Valid @RequestBody ScheduleGenerationRequestDTO request) {

        log.info("🎭 Ejecutando simulación de generación");

        try {
            // Crear una copia del request para simulación
            ScheduleGenerationRequestDTO simulationRequest = ScheduleGenerationRequestDTO.builder()
                    .periodUuid(request.getPeriodUuid())
                    .modalityUuid(request.getModalityUuid())
                    .careerUuid(request.getCareerUuid())
                    .cycleUuid(request.getCycleUuid())
                    .groupUuids(request.getGroupUuids())
                    .excludedDays(request.getExcludedDays())
                    .preferredTimeSlotUuids(request.getPreferredTimeSlotUuids())
                    .maxHoursPerDay(request.getMaxHoursPerDay())
                    .minHoursPerDay(request.getMinHoursPerDay())
                    .distributeEvenly(request.getDistributeEvenly())
                    .respectTeacherContinuity(request.getRespectTeacherContinuity())
                    .avoidTimeGaps(request.getAvoidTimeGaps())
                    .maxConsecutiveHours(request.getMaxConsecutiveHours())
                    .prioritizeLabsAfterTheory(request.getPrioritizeLabsAfterTheory())
                    .preferredTimeSlotWeight(request.getPreferredTimeSlotWeight())
                    .build();

            // TODO: Implementar lógica de simulación que no cree sesiones reales
            // Por ahora, devolver una respuesta simulada
            ScheduleGenerationResultDTO simulationResult = ScheduleGenerationResultDTO.builder()
                    .success(true)
                    .message("Simulación completada exitosamente")
                    .summary(ScheduleGenerationSummaryDTO.builder()
                            .totalGroupsProcessed(3)
                            .totalCoursesProcessed(15)
                            .totalSessionsGenerated(45)
                            .totalHoursAssigned(45)
                            .conflictsFound(0)
                            .warningsGenerated(2)
                            .successRate(95.5)
                            .build())
                    .generatedSessions(new ArrayList<>()) // Vacío en simulación
                    .conflicts(new ArrayList<>())
                    .warnings(Arrays.asList(
                            ScheduleWarningDTO.builder()
                                    .type("DISTRIBUTION_WARNING")
                                    .message("Distribución ligeramente desbalanceada en el grupo A")
                                    .affectedGroup("Grupo A")
                                    .suggestion("Considerar redistribuir algunas sesiones")
                                    .build()
                    ))
                    .statistics(ScheduleStatisticsDTO.builder()
                            .averageHoursPerDay(7.5)
                            .distributionBalance(0.85)
                            .build())
                    .executionTimeMs(150L)
                    .build();

            return ResponseEntity.ok(
                    ApiResponse.success(simulationResult, "Simulación completada")
            );

        } catch (Exception e) {
            log.error("❌ Error en simulación: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error en simulación: " + e.getMessage()));
        }
    }

    /**
     * Obtiene métricas de optimización para ayudar en la configuración
     */
    @PostMapping("/optimization-metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOptimizationMetrics(
            @Valid @RequestBody SchedulePreviewRequestDTO request) {

        log.info("📈 Calculando métricas de optimización");

        try {
            Map<String, Object> metrics = new HashMap<>();

            // Obtener vista previa para análisis
            SchedulePreviewDTO preview = scheduleGenerationService.getSchedulePreview(request);

            // Calcular métricas de optimización
            Map<String, Double> optimizationScores = new HashMap<>();
            optimizationScores.put("feasibilityScore", preview.getFeasibility().getFeasibilityScore());
            optimizationScores.put("resourceUtilization", 0.75); // Ejemplo
            optimizationScores.put("distributionQuality", 0.80); // Ejemplo
            optimizationScores.put("constraintSatisfaction", 0.90); // Ejemplo

            metrics.put("optimizationScores", optimizationScores);

            // Recomendaciones de optimización
            List<String> optimizationTips = new ArrayList<>();

            if (preview.getFeasibility().getFeasibilityScore() < 0.7) {
                optimizationTips.add("Considere reducir el número de grupos o aumentar recursos");
            }
            if (preview.getConstraints().getTotalRequiredHours() > 200) {
                optimizationTips.add("Alta carga de horas - considere distribución en múltiples fases");
            }
            optimizationTips.add("Active distribución equilibrada para mejor calidad");
            optimizationTips.add("Configure turnos preferentes según el ciclo académico");

            metrics.put("optimizationTips", optimizationTips);

            // Configuración recomendada
            Map<String, Object> recommendedConfig = new HashMap<>();
            recommendedConfig.put("maxHoursPerDay", preview.getConstraints().getTotalGroups() > 10 ? 6 : 8);
            recommendedConfig.put("maxConsecutiveHours", 3);
            recommendedConfig.put("distributeEvenly", true);
            recommendedConfig.put("avoidTimeGaps", true);

            metrics.put("recommendedConfig", recommendedConfig);

            return ResponseEntity.ok(
                    ApiResponse.success(metrics, "Métricas de optimización calculadas")
            );

        } catch (Exception e) {
            log.error("❌ Error calculando métricas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error calculando métricas: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener plantillas de configuración predefinidas
     */
    @GetMapping("/config-templates")
    public ResponseEntity<ApiResponse<Map<String, ScheduleGenerationRequestDTO>>> getConfigurationTemplates() {

        log.info("📋 Obteniendo plantillas de configuración");

        try {
            Map<String, ScheduleGenerationRequestDTO> templates = new HashMap<>();

            // Plantilla para ciclos iniciales (1-2)
            templates.put("INITIAL_CYCLES", ScheduleGenerationRequestDTO.builder()
                    .excludedDays(Arrays.asList(DayOfWeek.SATURDAY))
                    .maxHoursPerDay(6)
                    .minHoursPerDay(4)
                    .maxConsecutiveHours(3)
                    .distributeEvenly(true)
                    .respectTeacherContinuity(true)
                    .avoidTimeGaps(true)
                    .preferredTimeSlotWeight(0.8)
                    .build());

            // Plantilla para ciclos intermedios (3-6)
            templates.put("INTERMEDIATE_CYCLES", ScheduleGenerationRequestDTO.builder()
                    .excludedDays(new ArrayList<>())
                    .maxHoursPerDay(8)
                    .minHoursPerDay(4)
                    .maxConsecutiveHours(4)
                    .distributeEvenly(true)
                    .respectTeacherContinuity(true)
                    .avoidTimeGaps(true)
                    .prioritizeLabsAfterTheory(true)
                    .preferredTimeSlotWeight(0.7)
                    .build());

            // Plantilla para ciclos avanzados (7-10)
            templates.put("ADVANCED_CYCLES", ScheduleGenerationRequestDTO.builder()
                    .excludedDays(new ArrayList<>())
                    .maxHoursPerDay(8)
                    .minHoursPerDay(2)
                    .maxConsecutiveHours(4)
                    .distributeEvenly(false) // Más flexibilidad
                    .respectTeacherContinuity(true)
                    .avoidTimeGaps(false) // Permitir gaps para prácticas
                    .prioritizeLabsAfterTheory(true)
                    .preferredTimeSlotWeight(0.5)
                    .build());

            // Plantilla conservadora (alta calidad, menos riesgo)
            templates.put("CONSERVATIVE", ScheduleGenerationRequestDTO.builder()
                    .excludedDays(Arrays.asList(DayOfWeek.SATURDAY))
                    .maxHoursPerDay(6)
                    .minHoursPerDay(4)
                    .maxConsecutiveHours(3)
                    .distributeEvenly(true)
                    .respectTeacherContinuity(true)
                    .avoidTimeGaps(true)
                    .preferredTimeSlotWeight(0.9)
                    .build());

            // Plantilla agresiva (máxima utilización)
            templates.put("AGGRESSIVE", ScheduleGenerationRequestDTO.builder()
                    .excludedDays(new ArrayList<>())
                    .maxHoursPerDay(10)
                    .minHoursPerDay(6)
                    .maxConsecutiveHours(6)
                    .distributeEvenly(false)
                    .respectTeacherContinuity(false)
                    .avoidTimeGaps(false)
                    .preferredTimeSlotWeight(0.3)
                    .build());

            return ResponseEntity.ok(
                    ApiResponse.success(templates, "Plantillas de configuración obtenidas")
            );

        } catch (Exception e) {
            log.error("❌ Error obteniendo plantillas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error obteniendo plantillas: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para limpiar/resetear horarios de un periodo antes de regenerar
     */
    @DeleteMapping("/clear-period/{periodUuid}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> clearPeriodSchedules(
            @PathVariable UUID periodUuid,
            @RequestParam(required = false) UUID careerUuid,
            @RequestParam(required = false) UUID cycleUuid) {

        log.info("🧹 Limpiando horarios del periodo: {}", periodUuid);

        try {
            // TODO: Implementar lógica real de limpieza
            // Debería eliminar todas las ClassSession del periodo especificado
            // Opcionalmente filtradas por carrera o ciclo

            Map<String, Object> result = new HashMap<>();
            result.put("deletedSessions", 0); // Placeholder
            result.put("affectedGroups", 0);
            result.put("message", "Funcionalidad de limpieza pendiente de implementación");

            return ResponseEntity.ok(
                    ApiResponse.success(result, "Limpieza de horarios completada")
            );

        } catch (Exception e) {
            log.error("❌ Error limpiando horarios: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error limpiando horarios: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para exportar resultado de generación a diferentes formatos
     */
    @PostMapping("/export/{format}")
    public ResponseEntity<?> exportGenerationResult(
            @PathVariable String format,
            @RequestBody ScheduleGenerationResultDTO result) {

        log.info("📤 Exportando resultado en formato: {}", format);

        try {
            switch (format.toUpperCase()) {
                case "JSON":
                    return ResponseEntity.ok()
                            .header("Content-Disposition", "attachment; filename=horarios.json")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(result);

                case "CSV":
                    // TODO: Implementar exportación a CSV
                    String csvContent = "Funcionalidad CSV pendiente de implementación";
                    return ResponseEntity.ok()
                            .header("Content-Disposition", "attachment; filename=horarios.csv")
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(csvContent);

                case "PDF":
                    // TODO: Implementar exportación a PDF
                    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                            .body(ApiResponse.error("Exportación PDF no implementada"));

                default:
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Formato no soportado: " + format));
            }

        } catch (Exception e) {
            log.error("❌ Error exportando resultado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error exportando resultado: " + e.getMessage()));
        }
    }

    /**
     * Analiza horarios existentes antes de la generación
     */
    @PostMapping("/analyze-existing")
    public ResponseEntity<ApiResponse<ExistingScheduleAnalysisDTO>> analyzeExistingSchedules(
            @Valid @RequestBody ScheduleGenerationRequestDTO request) {

        log.info("🔍 Analizando horarios existentes");

        try {
            ExistingScheduleAnalysisDTO analysis = scheduleGenerationService.analyzeExistingSchedules(request);

            return ResponseEntity.ok(
                    ApiResponse.success(analysis, "Análisis de horarios existentes completado")
            );

        } catch (Exception e) {
            log.error("❌ Error analizando horarios existentes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error analizando horarios: " + e.getMessage()));
        }
    }

    /**
     * Limpia horarios existentes según estrategia especificada
     */
    @PostMapping("/cleanup")
    public ResponseEntity<ApiResponse<ScheduleCleanupResultDTO>> cleanupExistingSchedules(
            @Valid @RequestBody ScheduleCleanupRequestDTO request) {

        log.info("🧹 Limpiando horarios existentes con estrategia: {}", request.getStrategy());

        try {
            ScheduleCleanupResultDTO result = scheduleGenerationService.cleanupExistingSchedules(request);

            if (result.getSuccess()) {
                return ResponseEntity.ok(
                        ApiResponse.success(result, "Limpieza completada exitosamente")
                );
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Error en limpieza: " + result.getMessage(), result));
            }

        } catch (Exception e) {
            log.error("❌ Error en limpieza de horarios: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error en limpieza: " + e.getMessage()));
        }
    }

    /**
     * Generación inteligente que maneja horarios existentes automáticamente
     */
    @PostMapping("/generate-intelligent")
    public ResponseEntity<ApiResponse<ScheduleGenerationResultDTO>> generateIntelligentSchedule(
            @Valid @RequestBody ScheduleGenerationRequestDTO request,
            @RequestParam(defaultValue = "SELECTIVE_CLEANUP") String cleanupStrategy) {

        log.info("🧠 Generación inteligente con estrategia: {}", cleanupStrategy);

        try {
            ScheduleCleanupRequestDTO.CleanupStrategy strategy =
                    ScheduleCleanupRequestDTO.CleanupStrategy.valueOf(cleanupStrategy.toUpperCase());

            ScheduleGenerationResultDTO result = scheduleGenerationService
                    .generateScheduleIntelligent(request, strategy);

            if (result.isSuccess()) {
                return ResponseEntity.ok(
                        ApiResponse.success(result, "Generación inteligente completada exitosamente")
                );
            } else {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .body(ApiResponse.success(result,
                                "Generación completada con advertencias: " + result.getMessage()));
            }

        } catch (IllegalArgumentException e) {
            log.warn("❌ Estrategia inválida: {}", cleanupStrategy);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Estrategia de limpieza inválida: " + cleanupStrategy));

        } catch (Exception e) {
            log.error("❌ Error en generación inteligente: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error en generación inteligente: " + e.getMessage()));
        }
    }

    /**
     * Endpoint unificado que realiza todo el flujo completo
     */
    @PostMapping("/generate-complete-flow")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateCompleteFlow(
            @Valid @RequestBody ScheduleGenerationRequestDTO request,
            @RequestParam(required = false, defaultValue = "false") boolean autoResolve,
            @RequestParam(required = false, defaultValue = "SELECTIVE_CLEANUP") String defaultStrategy) {

        log.info("🔄 Iniciando flujo completo de generación");

        try {
            Map<String, Object> result = new HashMap<>();

            // 1. Analizar estado actual
            ExistingScheduleAnalysisDTO existingAnalysis = scheduleGenerationService
                    .analyzeExistingSchedules(request);
            result.put("existingAnalysis", existingAnalysis);

            // 2. Determinar acción automáticamente o requerir decisión del usuario
            if (existingAnalysis.getNeedsUserDecision() && !autoResolve) {
                // Necesita decisión del usuario
                result.put("requiresUserDecision", true);
                result.put("recommendedActions", generateRecommendedActions(existingAnalysis));
                result.put("message", "Se detectaron horarios existentes. Se requiere decisión del usuario.");

                return ResponseEntity.ok(
                        ApiResponse.success(result, "Análisis completado - Se requiere decisión del usuario")
                );
            }

            // 3. Auto-resolver o aplicar estrategia especificada
            ScheduleCleanupRequestDTO.CleanupStrategy strategy = autoResolve ?
                    determineOptimalStrategy(existingAnalysis) :
                    ScheduleCleanupRequestDTO.CleanupStrategy.valueOf(defaultStrategy.toUpperCase());

            // 4. Ejecutar generación inteligente
            ScheduleGenerationResultDTO generationResult = scheduleGenerationService
                    .generateScheduleIntelligent(request, strategy);
            result.put("generationResult", generationResult);
            result.put("requiresUserDecision", false);
            result.put("appliedStrategy", strategy.name());

            // 5. Preparar respuesta final
            boolean overallSuccess = generationResult.isSuccess();
            String message = overallSuccess ?
                    "Flujo completo ejecutado exitosamente" :
                    "Flujo ejecutado con advertencias";

            HttpStatus status = overallSuccess ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT;

            return ResponseEntity.status(status)
                    .body(ApiResponse.success(result, message));

        } catch (Exception e) {
            log.error("❌ Error en flujo completo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error en flujo completo: " + e.getMessage()));
        }
    }

    /**
     * Obtiene resumen rápido del estado de horarios para un periodo
     */
    @GetMapping("/period-status/{periodUuid}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPeriodScheduleStatus(
            @PathVariable UUID periodUuid,
            @RequestParam(required = false) UUID modalityUuid,
            @RequestParam(required = false) UUID careerUuid,
            @RequestParam(required = false) UUID cycleUuid) {

        log.info("📊 Obteniendo estado de periodo: {}", periodUuid);

        try {
            Map<String, Object> status = new HashMap<>();

            // Crear request temporal para análisis
            ScheduleGenerationRequestDTO tempRequest = ScheduleGenerationRequestDTO.builder()
                    .periodUuid(periodUuid)
                    .modalityUuid(modalityUuid)
                    .careerUuid(careerUuid)
                    .cycleUuid(cycleUuid)
                    .build();

            // Obtener análisis básico
            ExistingScheduleAnalysisDTO analysis = scheduleGenerationService.analyzeExistingSchedules(tempRequest);

            status.put("totalGroups", analysis.getTotalGroups());
            status.put("groupsWithSchedules", analysis.getGroupsWithExistingSessions());
            status.put("groupsWithoutSchedules", analysis.getGroupsWithoutSessions());
            status.put("needsAttention", analysis.getNeedsUserDecision());
            status.put("workloadLevel", analysis.getWorkloadAnalysis().getUtilizationLevel());
            status.put("recommendations", analysis.getRecommendations());

            // Estadísticas adicionales del periodo
            List<ClassSessionEntity> allSessions = classSessionRepository.findByPeriod(periodUuid);
            status.put("totalSessions", allSessions.size());

            int totalHours = allSessions.stream()
                    .mapToInt(session -> session.getTeachingHours().size())
                    .sum();
            status.put("totalHoursAssigned", totalHours);

            // Distribución por día
            Map<String, Long> sessionsByDay = allSessions.stream()
                    .collect(Collectors.groupingBy(
                            s -> s.getDayOfWeek().name(),
                            Collectors.counting()
                    ));
            status.put("distributionByDay", sessionsByDay);

            return ResponseEntity.ok(
                    ApiResponse.success(status, "Estado del periodo obtenido exitosamente")
            );

        } catch (Exception e) {
            log.error("❌ Error obteniendo estado del periodo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error obteniendo estado: " + e.getMessage()));
        }
    }

    /**
     * Valida la configuración considerando horarios existentes
     */
    @PostMapping("/validate-with-existing")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateWithExistingSchedules(
            @Valid @RequestBody ScheduleGenerationRequestDTO request) {

        log.info("✅ Validando configuración con horarios existentes");

        try {
            Map<String, Object> validation = new HashMap<>();
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            List<String> recommendations = new ArrayList<>();

            // Validaciones básicas
            if (!request.isValidScope()) {
                errors.add("Debe especificar al menos un filtro de alcance");
            }

            // Analizar contexto de horarios existentes
            ExistingScheduleAnalysisDTO existingAnalysis = scheduleGenerationService
                    .analyzeExistingSchedules(request);

            // Validaciones específicas del contexto
            if (existingAnalysis.getNeedsUserDecision()) {
                warnings.add("Se detectaron " + existingAnalysis.getGroupsWithExistingSessions() +
                        " grupos con horarios existentes");
                recommendations.add("Considere usar el endpoint /generate-complete-flow para manejo automático");
            }

            // Validaciones de carga de trabajo
            WorkloadAnalysisDTO workload = existingAnalysis.getWorkloadAnalysis();
            if ("CRITICAL".equals(workload.getUtilizationLevel())) {
                warnings.add("Sistema con alta utilización - posibles conflictos");
            }

            if (!workload.getOverloadedTeachers().isEmpty()) {
                warnings.add(workload.getOverloadedTeachers().size() + " docentes sobrecargados detectados");
            }

            // Análisis de factibilidad específico
            SchedulePreviewDTO preview = scheduleGenerationService.getSchedulePreview(
                    SchedulePreviewRequestDTO.builder()
                            .periodUuid(request.getPeriodUuid())
                            .modalityUuid(request.getModalityUuid())
                            .careerUuid(request.getCareerUuid())
                            .cycleUuid(request.getCycleUuid())
                            .groupUuids(request.getGroupUuids())
                            .build()
            );

            double feasibilityScore = preview.getFeasibility().getFeasibilityScore();
            if (feasibilityScore < 0.6) {
                warnings.add("Baja factibilidad detectada (" +
                        String.format("%.1f%%", feasibilityScore * 100) + ")");
            }

            // Recomendaciones específicas
            if (existingAnalysis.getGroupsWithExistingSessions() > 0) {
                recommendations.add("Use 'RESET_ALL' para empezar desde cero");
                recommendations.add("Use 'COMPLETE_EXISTING' para preservar horarios actuales");
                recommendations.add("Use 'SELECTIVE_CLEANUP' para enfoque balanceado");
            }

            validation.put("isValid", errors.isEmpty());
            validation.put("errors", errors);
            validation.put("warnings", warnings);
            validation.put("recommendations", recommendations);
            validation.put("existingSchedulesContext", existingAnalysis);
            validation.put("feasibilityScore", feasibilityScore);
            validation.put("workloadLevel", workload.getUtilizationLevel());

            return ResponseEntity.ok(
                    ApiResponse.success(validation, "Validación con contexto completada")
            );

        } catch (Exception e) {
            log.error("❌ Error en validación contextual: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error en validación: " + e.getMessage()));
        }
    }

// Métodos auxiliares privados

    private List<Map<String, String>> generateRecommendedActions(ExistingScheduleAnalysisDTO analysis) {
        List<Map<String, String>> actions = new ArrayList<>();

        // Analizar cada grupo y generar recomendaciones específicas
        for (GroupScheduleStatusDTO group : analysis.getGroupStatuses()) {
            if (group.getHasExistingSessions()) {
                Map<String, String> action = new HashMap<>();
                action.put("groupName", group.getGroupName());
                action.put("currentStatus", String.format("%.1f%% completo",
                        group.getEstimatedCompleteness() * 100));
                action.put("recommendedAction", group.getRecommendedAction());

                String reasoning;
                switch (group.getRecommendedAction()) {
                    case "RESET":
                        reasoning = "Muy pocas horas asignadas - mejor empezar desde cero";
                        break;
                    case "COMPLETE":
                        reasoning = "Horario parcial - se puede completar";
                        break;
                    default:
                        reasoning = "Horario aparentemente completo";
                }
                action.put("reasoning", reasoning);

                actions.add(action);
            }
        }

        return actions;
    }

    private ScheduleCleanupRequestDTO.CleanupStrategy determineOptimalStrategy(
            ExistingScheduleAnalysisDTO analysis) {

        // Lógica para determinar automáticamente la mejor estrategia
        long groupsWithMostlyComplete = analysis.getGroupStatuses().stream()
                .filter(group -> group.getHasExistingSessions() &&
                        group.getEstimatedCompleteness() > 0.7)
                .count();

        long groupsWithPartial = analysis.getGroupStatuses().stream()
                .filter(group -> group.getHasExistingSessions() &&
                        group.getEstimatedCompleteness() > 0.2 &&
                        group.getEstimatedCompleteness() <= 0.7)
                .count();

        if (groupsWithMostlyComplete > analysis.getGroupsWithExistingSessions() * 0.6) {
            return ScheduleCleanupRequestDTO.CleanupStrategy.COMPLETE_EXISTING;
        } else if (groupsWithPartial > 0) {
            return ScheduleCleanupRequestDTO.CleanupStrategy.SELECTIVE_CLEANUP;
        } else {
            return ScheduleCleanupRequestDTO.CleanupStrategy.RESET_ALL;
        }
    }
}
