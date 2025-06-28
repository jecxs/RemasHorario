package com.pontificia.remashorario.modules.teacher;

import com.pontificia.remashorario.modules.KnowledgeArea.KnowledgeAreaEntity;
import com.pontificia.remashorario.modules.KnowledgeArea.KnowledgeAreaService;
import com.pontificia.remashorario.modules.TimeSlot.TimeSlotEntity;
import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentEntity;
import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentService;
import com.pontificia.remashorario.modules.classSession.ClassSessionEntity;
import com.pontificia.remashorario.modules.classSession.ClassSessionRepository;
import com.pontificia.remashorario.modules.course.CourseEntity;
import com.pontificia.remashorario.modules.teacher.dto.*;
import com.pontificia.remashorario.modules.teacher.mapper.TeacherMapper;

import com.pontificia.remashorario.modules.teacherAvailability.TeacherAvailabilityEntity;
import com.pontificia.remashorario.modules.teacherAvailability.TeacherAvailabilityRepository;
import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherWithAvailabilitiesDTO;
import com.pontificia.remashorario.modules.teacherAvailability.mapper.TeacherAvailabilityMapper;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourRepository;
import com.pontificia.remashorario.modules.user.UserService;
import com.pontificia.remashorario.modules.course.CourseService;
import com.pontificia.remashorario.modules.TimeSlot.TimeSlotService;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherService extends BaseService<TeacherEntity> {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;
    private final AcademicDepartmentService departmentService;
    private final KnowledgeAreaService knowledgeAreaService;
    private final UserService userService;
    private final CourseService courseService;
    private final TimeSlotService timeSlotService;
    private final TeacherAvailabilityRepository teacherAvailabilityRepository;
    private final TeacherAvailabilityMapper teacherAvailabilityMapper;
    private final TeachingHourRepository teachingHourRepository;
    private final ClassSessionRepository classSessionRepository;

    @Autowired
    public TeacherService(TeacherRepository teacherRepository,
                          TeacherMapper teacherMapper,
                          AcademicDepartmentService departmentService,
                          KnowledgeAreaService knowledgeAreaService,
                          UserService userService,
                          CourseService courseService,
                          TimeSlotService timeSlotService,
                          TeacherAvailabilityRepository teacherAvailabilityRepository, TeacherAvailabilityMapper teacherAvailabilityMapper, TeachingHourRepository teachingHourRepository, ClassSessionRepository classSessionRepository) {
        super(teacherRepository);
        this.teacherRepository = teacherRepository;
        this.teacherMapper = teacherMapper;
        this.departmentService = departmentService;
        this.knowledgeAreaService = knowledgeAreaService;
        this.userService = userService;
        this.courseService = courseService;
        this.timeSlotService = timeSlotService;
        this.teacherAvailabilityRepository = teacherAvailabilityRepository;
        this.teacherAvailabilityMapper = teacherAvailabilityMapper;
        this.teachingHourRepository = teachingHourRepository;
        this.classSessionRepository = classSessionRepository;
    }

    public List<TeacherResponseDTO> getAllTeachers() {
        List<TeacherEntity> teachers = findAll();
        return teacherMapper.toResponseDTOList(teachers);
    }

    public TeacherResponseDTO getTeacherById(UUID uuid) {
        TeacherEntity teacher = findTeacherOrThrow(uuid);
        return teacherMapper.toResponseDTO(teacher);
    }


    // ✅ NUEVO: Verificar conflictos de clases asignadas
    private TeacherClassConflictInfo checkTeacherClassConflicts(
            TeacherEntity teacher, String dayOfWeek, List<UUID> specificHourUuids) {

        try {
            // Buscar sesiones existentes del docente en el día específico
            List<ClassSessionEntity> teacherSessions = classSessionRepository
                    .findByTeacherUuidAndDayOfWeek(teacher.getUuid(), DayOfWeek.valueOf(dayOfWeek.toUpperCase()));

            List<TeacherClassConflictDTO> conflicts = new ArrayList<>();
            Set<UUID> conflictingHourUuids = new HashSet<>();

            for (ClassSessionEntity session : teacherSessions) {
                // Verificar si alguna hora pedagógica de la sesión existente
                // coincide con las horas que queremos asignar
                List<UUID> sessionHourUuids = session.getTeachingHours().stream()
                        .map(TeachingHourEntity::getUuid)
                        .collect(Collectors.toList());

                // Encontrar intersección
                List<UUID> intersection = sessionHourUuids.stream()
                        .filter(specificHourUuids::contains)
                        .collect(Collectors.toList());

                if (!intersection.isEmpty()) {
                    // Hay conflicto - construir información detallada
                    TeachingHourEntity firstHour = session.getTeachingHours().stream()
                            .min(Comparator.comparing(TeachingHourEntity::getOrderInTimeSlot))
                            .orElse(null);
                    TeachingHourEntity lastHour = session.getTeachingHours().stream()
                            .max(Comparator.comparing(TeachingHourEntity::getOrderInTimeSlot))
                            .orElse(null);

                    TeacherClassConflictDTO conflict = TeacherClassConflictDTO.builder()
                            .sessionUuid(session.getUuid().toString())
                            .courseName(session.getCourse().getName())
                            .courseCode(session.getCourse().getCode())
                            .studentGroupName(session.getStudentGroup().getName())
                            .studentGroupUuid(session.getStudentGroup().getUuid().toString()) // ✅ AGREGAR ESTA LÍNEA
                            .dayOfWeek(session.getDayOfWeek().name())
                            .startTime(firstHour != null ? firstHour.getStartTime().toString() : "")
                            .endTime(lastHour != null ? lastHour.getEndTime().toString() : "")
                            .learningSpaceName(session.getLearningSpace().getName())
                            .sessionType(session.getSessionType().getName().name())
                            .conflictingHourUuids(intersection.stream().map(UUID::toString).collect(Collectors.toList()))
                            .build();

                    conflicts.add(conflict);
                    conflictingHourUuids.addAll(intersection);

                    // ✅ AGREGAR LOG PARA DEBUGGING
                    System.out.println("=== CONFLICT DETECTED ===");
                    System.out.println("Teacher: " + teacher.getFullName());
                    System.out.println("Conflicting session: " + session.getCourse().getName());
                    System.out.println("Student group: " + session.getStudentGroup().getName());
                    System.out.println("Student group UUID: " + session.getStudentGroup().getUuid());
                    System.out.println("Conflict DTO student group UUID: " + conflict.getStudentGroupUuid());
                }
            }

            // Determinar tipo de conflicto
            String conflictType = "NONE";
            if (!conflicts.isEmpty()) {
                if (conflictingHourUuids.size() == specificHourUuids.size()) {
                    conflictType = "FULL_CONFLICT"; // Todas las horas en conflicto
                } else {
                    conflictType = "PARTIAL_CONFLICT"; // Solo algunas horas en conflicto
                }
            }

            return TeacherClassConflictInfo.builder()
                    .hasConflict(!conflicts.isEmpty())
                    .conflicts(conflicts)
                    .conflictType(conflictType)
                    .conflictingHourUuids(conflictingHourUuids.stream().map(UUID::toString).collect(Collectors.toList()))
                    .build();

        } catch (Exception e) {
            System.out.println("Error checking teacher conflicts: " + e.getMessage());
            e.printStackTrace(); // ✅ Agregar stack trace para debugging
            return TeacherClassConflictInfo.builder()
                    .hasConflict(false)
                    .conflicts(new ArrayList<>())
                    .conflictType("ERROR")
                    .build();
        }
    }
    // ✅ NUEVO: Generar resumen de conflictos
    private String generateConflictSummary(List<TeacherClassConflictDTO> conflicts) {
        if (conflicts.isEmpty()) return "";

        if (conflicts.size() == 1) {
            TeacherClassConflictDTO conflict = conflicts.get(0);
            return String.format("Clase de %s con grupo %s",
                    conflict.getCourseName(), conflict.getStudentGroupName());
        }

        return String.format("%d clases en conflicto", conflicts.size());
    }


    public TeacherWithAvailabilitiesDTO getTeacherWithAvailabilities(UUID uuid) {
        TeacherEntity teacher = teacherRepository.findByIdWithAvailabilities(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Docente no encontrado con ID: " + uuid));
        return teacherMapper.toWithAvailabilitiesDTO(teacher);
    }

    public TeacherEntity findTeacherOrThrow(UUID uuid) {
        return findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Docente no encontrado con ID: " + uuid));
    }

    public List<TeacherEligibilityResponseDTO> getEligibleTeachersWithAvailability(
            UUID courseUuid, String dayOfWeek, UUID timeSlotUuid, List<UUID> specificHourUuids) { // ✅ NUEVO PARÁMETRO

        CourseEntity course = courseService.findCourseOrThrow(courseUuid);
        List<TeacherEntity> eligibleTeachers = teacherRepository
                .findByKnowledgeAreasContaining(course.getTeachingKnowledgeArea().getUuid());

        List<TeacherEligibilityResponseDTO> result = new ArrayList<>();

        for (TeacherEntity teacher : eligibleTeachers) {
            TeacherEligibilityResponseDTO dto = buildTeacherEligibilityResponse(
                    teacher, dayOfWeek, timeSlotUuid, specificHourUuids); // ✅ PASAR HORAS
            result.add(dto);
        }

        result.sort((a, b) -> {
            if (a.getIsAvailableForTimeSlot() && !b.getIsAvailableForTimeSlot()) return -1;
            if (!a.getIsAvailableForTimeSlot() && b.getIsAvailableForTimeSlot()) return 1;
            return a.getFullName().compareTo(b.getFullName());
        });

        return result;
    }


    // ✅ MÉTODO ACTUALIZADO
    // ✅ ACTUALIZAR: buildTeacherEligibilityResponse para incluir conflictos
    private TeacherEligibilityResponseDTO buildTeacherEligibilityResponse(
            TeacherEntity teacher, String dayOfWeek, UUID timeSlotUuid, List<UUID> specificHourUuids) {

        TeacherResponseDTO basicInfo = teacherMapper.toResponseDTO(teacher);

        boolean isAvailable = false;
        String status = "NOT_AVAILABLE";
        List<TeacherAvailabilityEntity> dayAvailabilities = new ArrayList<>();
        String recommendedSlots = "";

        // ✅ NUEVO: Verificar conflictos de clases
        TeacherClassConflictInfo conflictInfo = null;
        if (dayOfWeek != null && specificHourUuids != null && !specificHourUuids.isEmpty()) {
            conflictInfo = checkTeacherClassConflicts(teacher, dayOfWeek, specificHourUuids);
        }

        if (dayOfWeek != null) {
            try {
                dayAvailabilities = teacherAvailabilityRepository
                        .findByTeacherAndDayOfWeek(teacher, DayOfWeek.valueOf(dayOfWeek.toUpperCase()));

                if (dayAvailabilities.isEmpty()) {
                    status = "NO_SCHEDULE_CONFIGURED";
                    recommendedSlots = "Sin horario configurado";
                } else {
                    // ✅ CAMBIO: Considerar tanto disponibilidad como conflictos
                    if (specificHourUuids != null && !specificHourUuids.isEmpty()) {
                        isAvailable = isTeacherAvailableForSpecificHours(teacher, dayOfWeek, specificHourUuids);

                        // ✅ NUEVA LÓGICA: Determinar estado final considerando conflictos
                        if (conflictInfo != null && conflictInfo.isHasConflict()) {
                            if ("FULL_CONFLICT".equals(conflictInfo.getConflictType())) {
                                status = "SCHEDULE_CONFLICT";
                                isAvailable = false;
                            } else if ("PARTIAL_CONFLICT".equals(conflictInfo.getConflictType())) {
                                status = "PARTIAL_CONFLICT";
                                isAvailable = false; // No permitir selección si hay conflicto parcial
                            }
                        } else if (isAvailable) {
                            status = "AVAILABLE";
                        } else {
                            status = "TIME_CONFLICT";
                        }
                    } else if (timeSlotUuid != null) {
                        // Lógica anterior para timeSlot
                        TimeSlotEntity timeSlot = timeSlotService.findOrThrow(timeSlotUuid);
                        isAvailable = isTeacherAvailableForAnyHourInTimeSlot(teacher, dayOfWeek, timeSlot);
                        status = isAvailable ? "AVAILABLE" : "TIME_CONFLICT";
                    } else {
                        isAvailable = dayAvailabilities.stream()
                                .anyMatch(av -> av.getIsAvailable() != null && av.getIsAvailable());
                        status = isAvailable ? "AVAILABLE" : "NOT_AVAILABLE";
                    }

                    recommendedSlots = generateRecommendedTimeSlots(dayAvailabilities);
                }
            } catch (Exception e) {
                status = "ERROR";
                recommendedSlots = "Error al verificar disponibilidad";
            }
        }

        // ✅ CONSTRUIR RESPUESTA COMPLETA con información de conflictos
        TeacherEligibilityResponseDTO.TeacherEligibilityResponseDTOBuilder builder = TeacherEligibilityResponseDTO.builder()
                .uuid(basicInfo.getUuid())
                .fullName(basicInfo.getFullName())
                .email(basicInfo.getEmail())
                .department(basicInfo.getDepartment())
                .knowledgeAreas(basicInfo.getKnowledgeAreas())
                .hasUserAccount(basicInfo.getHasUserAccount())
                .isAvailableForTimeSlot(isAvailable)
                .availabilityStatus(status)
                .availabilitiesForDay(teacherAvailabilityMapper.toResponseDTOList(dayAvailabilities))
                .recommendedTimeSlots(recommendedSlots);

        // ✅ AGREGAR información de conflictos si existe
        if (conflictInfo != null) {
            builder.hasScheduleConflict(conflictInfo.isHasConflict())
                    .conflictingClasses(conflictInfo.getConflicts())
                    .conflictType(conflictInfo.getConflictType());

            if (conflictInfo.isHasConflict()) {
                String conflictSummary = generateConflictSummary(conflictInfo.getConflicts());
                builder.conflictSummary(conflictSummary);
            }
        }

        return builder.build();
    }

    // ✅ NUEVO MÉTODO: Verificar disponibilidad para horas específicas por UUID
    private boolean isTeacherAvailableForSpecificHours(TeacherEntity teacher, String dayOfWeek, List<UUID> hourUuids) {
        try {
            // Obtener las horas pedagógicas
            List<TeachingHourEntity> teachingHours = hourUuids.stream()
                    .map(uuid -> teachingHourRepository.findById(uuid)
                            .orElseThrow(() -> new EntityNotFoundException("Teaching hour not found: " + uuid)))
                    .collect(Collectors.toList());

            // Obtener disponibilidades del docente
            List<TeacherAvailabilityEntity> availabilities = teacherAvailabilityRepository
                    .findByTeacherAndDayOfWeek(teacher, DayOfWeek.valueOf(dayOfWeek.toUpperCase()));

            // Verificar que todas las horas estén cubiertas por las disponibilidades
            for (TeachingHourEntity hour : teachingHours) {
                boolean hourCovered = availabilities.stream().anyMatch(availability ->
                        availability.getIsAvailable() != null &&
                                availability.getIsAvailable() &&
                                hour.getStartTime().compareTo(availability.getStartTime()) >= 0 &&
                                hour.getEndTime().compareTo(availability.getEndTime()) <= 0
                );

                if (!hourCovered) {
                    System.out.println("Hour " + hour.getStartTime() + "-" + hour.getEndTime() +
                            " not covered for " + teacher.getFullName());
                    return false;
                }
            }

            System.out.println("All specific hours covered for " + teacher.getFullName());
            return true;

        } catch (Exception e) {
            System.out.println("Error checking specific hours for " + teacher.getFullName() + ": " + e.getMessage());
            return false;
        }
    }

    // ✅ MÉTODO AUXILIAR: Verificar si el docente está disponible para ALGUNA hora del turno
    private boolean isTeacherAvailableForAnyHourInTimeSlot(
            TeacherEntity teacher, String dayOfWeek, TimeSlotEntity timeSlot) {

        try {
            List<TeacherAvailabilityEntity> availabilities = teacherAvailabilityRepository
                    .findByTeacherAndDayOfWeek(teacher, DayOfWeek.valueOf(dayOfWeek.toUpperCase()));

            // Verificar si ALGUNA hora pedagógica del turno está dentro de la disponibilidad
            return timeSlot.getTeachingHours().stream().anyMatch(teachingHour ->
                    availabilities.stream().anyMatch(availability ->
                            availability.getIsAvailable() != null &&
                                    availability.getIsAvailable() &&
                                    teachingHour.getStartTime().compareTo(availability.getStartTime()) >= 0 &&
                                    teachingHour.getEndTime().compareTo(availability.getEndTime()) <= 0
                    )
            );

        } catch (Exception e) {
            System.out.println("Error checking availability for any hour in timeslot: " + e.getMessage());
            return false;
        }
    }

    private boolean isTeacherAvailableInSpecificTimeSlot(TeacherEntity teacher, String dayOfWeek, TimeSlotEntity timeSlot) {
        try {
            List<TeacherAvailabilityEntity> availabilities = teacherAvailabilityRepository
                    .findByTeacherAndDayOfWeek(teacher, DayOfWeek.valueOf(dayOfWeek.toUpperCase()));

            return availabilities.stream().anyMatch(availability ->
                    availability.getIsAvailable() != null &&
                            availability.getIsAvailable() &&
                            timeSlot.getStartTime().compareTo(availability.getStartTime()) >= 0 &&
                            timeSlot.getEndTime().compareTo(availability.getEndTime()) <= 0
            );
        } catch (Exception e) {
            return false;
        }
    }
    private String generateRecommendedTimeSlots(List<TeacherAvailabilityEntity> availabilities) {
        return availabilities.stream()
                .filter(av -> av.getIsAvailable() != null && av.getIsAvailable())
                .map(av -> av.getStartTime().toString().substring(0, 5) + "-" +
                        av.getEndTime().toString().substring(0, 5))
                .collect(Collectors.joining(", "));
    }

    // En TeacherService - método getEligibleTeachers
    public List<TeacherResponseDTO> getEligibleTeachers(UUID courseUuid, String dayOfWeek, UUID timeSlotUuid) {
        CourseEntity course = courseService.findCourseOrThrow(courseUuid);

        if (course.getTeachingKnowledgeArea() == null) {
            throw new IllegalStateException("El curso no tiene área de conocimiento definida");
        }

        // PASO 1: Obtener docentes por área de conocimiento
        List<TeacherEntity> eligibleTeachers = teacherRepository
                .findByKnowledgeAreasContaining(course.getTeachingKnowledgeArea().getUuid());

        System.out.println("=== DEBUG TEACHERS ===");
        System.out.println("Teachers by knowledge area: " + eligibleTeachers.size());
        System.out.println("DayOfWeek filter: " + dayOfWeek);
        System.out.println("TimeSlot filter: " + timeSlotUuid);

        // PASO 2: Filtrar por día con logs
        if (dayOfWeek != null && !dayOfWeek.trim().isEmpty()) {
            System.out.println("Applying day filter for: " + dayOfWeek);

            List<TeacherEntity> availableTeachers = new ArrayList<>();

            for (TeacherEntity teacher : eligibleTeachers) {
                boolean available = isTeacherAvailableOnDayWithLogs(teacher, dayOfWeek);
                System.out.println("Teacher: " + teacher.getFullName() + " available on " + dayOfWeek + ": " + available);

                if (available) {
                    availableTeachers.add(teacher);
                }
            }

            System.out.println("Teachers available after day filter: " + availableTeachers.size());
            eligibleTeachers = availableTeachers;
        }

        // PASO 3: Filtrar por turno (solo si pasó el filtro de día)
        if (timeSlotUuid != null && dayOfWeek != null && !eligibleTeachers.isEmpty()) {
            System.out.println("Applying time slot filter...");
            TimeSlotEntity timeSlot = timeSlotService.findOrThrow(timeSlotUuid);
            eligibleTeachers = eligibleTeachers.stream()
                    .filter(teacher -> isTeacherAvailableInTimeSlotWithLogs(teacher, dayOfWeek, timeSlot))
                    .collect(Collectors.toList());
            System.out.println("Teachers available after time slot filter: " + eligibleTeachers.size());
        }

        System.out.println("Final eligible teachers: " + eligibleTeachers.size());
        System.out.println("=== END DEBUG ===");

        return teacherMapper.toResponseDTOList(eligibleTeachers);
    }


    public List<TeacherEntity> getTeachersByKnowledgeArea(UUID knowledgeAreaUuid) {
        KnowledgeAreaEntity knowledgeArea = knowledgeAreaService.findOrThrow(knowledgeAreaUuid);
        return teacherRepository.findByKnowledgeAreasContaining(knowledgeArea.getUuid());
    }


    /// Método con logs para debuggear
    private boolean isTeacherAvailableOnDayWithLogs(TeacherEntity teacher, String dayOfWeek) {
        if (dayOfWeek == null || dayOfWeek.trim().isEmpty()) {
            System.out.println("  -> No day filter, returning true");
            return true;
        }

        try {
            List<TeacherAvailabilityEntity> availabilities = teacherAvailabilityRepository
                    .findByTeacherAndDayOfWeek(teacher, DayOfWeek.valueOf(dayOfWeek.toUpperCase()));

            System.out.println("  -> Teacher " + teacher.getFullName() + " has " + availabilities.size() + " availabilities for " + dayOfWeek);

            // CAMBIO IMPORTANTE: Si no tiene disponibilidades, lo incluimos
            if (availabilities.isEmpty()) {
                System.out.println("  -> No availabilities found, INCLUDING teacher (assuming available)");
                return true;  // ESTE ES EL CAMBIO CLAVE
            }

            // Verificar si tiene al menos una disponibilidad activa
            boolean hasActiveAvailability = availabilities.stream().anyMatch(availability -> {
                boolean isAvailable = availability != null &&
                        availability.getIsAvailable() != null &&
                        availability.getIsAvailable();

                System.out.println("    -> Availability: " + availability.getStartTime() + "-" + availability.getEndTime() +
                        " isAvailable: " + availability.getIsAvailable() + " -> " + isAvailable);

                return isAvailable;
            });

            System.out.println("  -> Has active availability: " + hasActiveAvailability);
            return hasActiveAvailability;

        } catch (IllegalArgumentException e) {
            System.out.println("  -> Invalid day format: " + dayOfWeek + ", INCLUDING teacher");
            return true;
        }
    }

    // Método con logs para turno
    private boolean isTeacherAvailableInTimeSlotWithLogs(TeacherEntity teacher, String dayOfWeek, TimeSlotEntity timeSlot) {
        if (dayOfWeek == null || timeSlot == null) return true;

        try {
            List<TeacherAvailabilityEntity> availabilities = teacherAvailabilityRepository
                    .findByTeacherAndDayOfWeek(teacher, DayOfWeek.valueOf(dayOfWeek.toUpperCase()));

            System.out.println("  -> Checking time slot for " + teacher.getFullName());
            System.out.println("  -> Time slot: " + timeSlot.getStartTime() + "-" + timeSlot.getEndTime());

            // Si no tiene disponibilidades, lo incluimos
            if (availabilities.isEmpty()) {
                System.out.println("  -> No availabilities for time slot check, INCLUDING");
                return true;
            }

            boolean fits = availabilities.stream().anyMatch(availability -> {
                boolean isAvailable = availability != null &&
                        availability.getIsAvailable() != null &&
                        availability.getIsAvailable() &&
                        timeSlot.getStartTime() != null &&
                        timeSlot.getEndTime() != null &&
                        availability.getStartTime() != null &&
                        availability.getEndTime() != null &&
                        timeSlot.getStartTime().compareTo(availability.getStartTime()) >= 0 &&
                        timeSlot.getEndTime().compareTo(availability.getEndTime()) <= 0;

                System.out.println("    -> Availability " + availability.getStartTime() + "-" + availability.getEndTime() +
                        " fits time slot: " + isAvailable);

                return isAvailable;
            });

            System.out.println("  -> Time slot fits: " + fits);
            return fits;

        } catch (Exception e) {
            System.out.println("  -> Error in time slot check: " + e.getMessage() + ", INCLUDING");
            return true;
        }
    }


    @Transactional
    public TeacherResponseDTO createTeacher(TeacherRequestDTO dto) {
        // Verificar email único
        if (teacherRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un docente con el email: " + dto.getEmail());
        }

        // Obtener entidades relacionadas
        AcademicDepartmentEntity department = departmentService.findDepartmentOrThrow(dto.getDepartmentUuid());
        Set<KnowledgeAreaEntity> knowledgeAreas = getKnowledgeAreasFromUuids(dto.getKnowledgeAreaUuids());

        // Validar que las áreas de conocimiento pertenezcan al departamento
        validateKnowledgeAreasBelongToDepartment(knowledgeAreas, department);

        // Crear y guardar
        TeacherEntity teacher = teacherMapper.toEntity(dto, department, knowledgeAreas);
        TeacherEntity savedTeacher = save(teacher);

        // Crear cuenta de usuario para el docente con contraseña por defecto
        userService.createTeacherUser(savedTeacher, "cambio123");

        return teacherMapper.toResponseDTO(savedTeacher);
    }

    @Transactional
    public TeacherResponseDTO updateTeacher(UUID uuid, TeacherUpdateDTO dto) {
        TeacherEntity teacher = findTeacherOrThrow(uuid);

        // Obtener entidades relacionadas
        AcademicDepartmentEntity department = departmentService.findDepartmentOrThrow(dto.getDepartmentUuid());
        Set<KnowledgeAreaEntity> knowledgeAreas = getKnowledgeAreasFromUuids(dto.getKnowledgeAreaUuids());

        // Validar que las áreas de conocimiento pertenezcan al departamento
        validateKnowledgeAreasBelongToDepartment(knowledgeAreas, department);

        // Actualizar
        teacherMapper.updateEntityFromDTO(teacher, dto, department, knowledgeAreas);
        TeacherEntity updatedTeacher = save(teacher);

        return teacherMapper.toResponseDTO(updatedTeacher);
    }

    @Transactional
    public void deleteTeacher(UUID uuid) {
        TeacherEntity teacher = findTeacherOrThrow(uuid);

        // Verificar si tiene asignaciones de horario (cuando se implemente)
        // TODO: Verificar asignaciones cuando se implemente ScheduleAssignment

        // Si tiene cuenta de usuario, solo desactivar
        if (teacher.getHasUserAccount()) {
            throw new IllegalStateException("No se puede eliminar un docente con cuenta de usuario activa");
        }

        deleteById(uuid);
    }

    public List<TeacherResponseDTO> filterTeachers(TeacherFilterDTO filters) {
        List<TeacherEntity> teachers;

        if (filters.getDepartmentUuid() != null && filters.getKnowledgeAreaUuids() != null && !filters.getKnowledgeAreaUuids().isEmpty()) {
            teachers = teacherRepository.findByDepartmentAndKnowledgeAreas(
                    filters.getDepartmentUuid(), filters.getKnowledgeAreaUuids());
        } else if (filters.getDepartmentUuid() != null) {
            teachers = teacherRepository.findByDepartmentUuid(filters.getDepartmentUuid());
        } else if (filters.getKnowledgeAreaUuids() != null && !filters.getKnowledgeAreaUuids().isEmpty()) {
            teachers = teacherRepository.findByKnowledgeAreaUuids(filters.getKnowledgeAreaUuids());
        } else if (filters.getSearchTerm() != null && !filters.getSearchTerm().trim().isEmpty()) {
            teachers = teacherRepository.searchByNameOrEmail(filters.getSearchTerm());
        } else if (filters.getHasUserAccount() != null) {
            teachers = teacherRepository.findByHasUserAccount(filters.getHasUserAccount());
        } else {
            teachers = findAll();
        }

        return teacherMapper.toResponseDTOList(teachers);
    }

    public List<TeacherResponseDTO> getTeachersByDepartment(UUID departmentUuid) {
        List<TeacherEntity> teachers = teacherRepository.findByDepartmentUuid(departmentUuid);
        return teacherMapper.toResponseDTOList(teachers);
    }

    /**
     * Obtiene docentes sugeridos para un curso basándose en el departamento
     */
    public List<TeacherResponseDTO> getSuggestedTeachersForCourse(UUID courseUuid) {
        // TODO: Implementar cuando se tenga la relación Course-Department
        // Por ahora devuelve todos los docentes
        return getAllTeachers();
    }

    private Set<KnowledgeAreaEntity> getKnowledgeAreasFromUuids(List<UUID> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return new HashSet<>();
        }
        return uuids.stream()
                .map(knowledgeAreaService::findKnowledgeAreaOrThrow)
                .collect(Collectors.toSet());
    }

    private void validateKnowledgeAreasBelongToDepartment(Set<KnowledgeAreaEntity> knowledgeAreas,
                                                          AcademicDepartmentEntity department) {
        for (KnowledgeAreaEntity area : knowledgeAreas) {
            if (!area.getDepartment().getUuid().equals(department.getUuid())) {
                throw new IllegalArgumentException(
                        "El área de conocimiento '" + area.getName() +
                                "' no pertenece al departamento seleccionado");
            }
        }
    }
}

