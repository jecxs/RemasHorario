package com.pontificia.remashorario.modules.extraAssignment;

import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeEntity;
import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeService;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teacher.TeacherService;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing extra assignments (workshops, substitute exams, etc.)
 * These are activities outside the regular schedule
 */
@Service
public class ExtraAssignmentService extends BaseService<ExtraAssignmentEntity> {

    private final ExtraAssignmentRepository extraAssignmentRepository;
    private final TeacherService teacherService;
    private final AttendanceActivityTypeService activityTypeService;

    @Autowired
    public ExtraAssignmentService(ExtraAssignmentRepository extraAssignmentRepository,
                                 TeacherService teacherService,
                                 AttendanceActivityTypeService activityTypeService) {
        super(extraAssignmentRepository);
        this.extraAssignmentRepository = extraAssignmentRepository;
        this.teacherService = teacherService;
        this.activityTypeService = activityTypeService;
    }

    public List<ExtraAssignmentEntity> getAllAssignments() {
        return findAll();
    }

    public ExtraAssignmentEntity getAssignmentById(UUID uuid) {
        return findAssignmentOrThrow(uuid);
    }

    public ExtraAssignmentEntity getAssignmentByIdWithDetails(UUID uuid) {
        return extraAssignmentRepository.findByIdWithDetails(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Asignación extra no encontrada con ID: " + uuid));
    }

    public List<ExtraAssignmentEntity> getAssignmentsByTeacher(UUID teacherUuid) {
        return extraAssignmentRepository.findByTeacherUuid(teacherUuid);
    }

    public List<ExtraAssignmentEntity> getAssignmentsByTeacherAndDate(UUID teacherUuid, LocalDate date) {
        return extraAssignmentRepository.findByTeacherUuidAndAssignmentDate(teacherUuid, date);
    }

    public List<ExtraAssignmentEntity> getAssignmentsByTeacherAndDateRange(UUID teacherUuid,
                                                                           LocalDate startDate,
                                                                           LocalDate endDate) {
        return extraAssignmentRepository.findByTeacherAndDateRange(teacherUuid, startDate, endDate);
    }

    public List<ExtraAssignmentEntity> getAssignmentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return extraAssignmentRepository.findByDateRange(startDate, endDate);
    }

    public List<ExtraAssignmentEntity> getAssignmentsByActivityType(UUID activityTypeUuid) {
        return extraAssignmentRepository.findByActivityTypeUuid(activityTypeUuid);
    }

    public ExtraAssignmentEntity findAssignmentOrThrow(UUID uuid) {
        return findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Asignación extra no encontrada con ID: " + uuid));
    }

    @Transactional
    public ExtraAssignmentEntity createAssignment(UUID teacherUuid, UUID activityTypeUuid,
                                                  String title, LocalDate assignmentDate,
                                                  LocalTime startTime, LocalTime endTime,
                                                  BigDecimal ratePerHour, String notes) {
        // Validate teacher exists
        TeacherEntity teacher = teacherService.findTeacherOrThrow(teacherUuid);

        // Validate activity type exists
        AttendanceActivityTypeEntity activityType = activityTypeService.findActivityTypeOrThrow(activityTypeUuid);

        // Validate times
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }

        // Calculate duration in minutes
        int durationMinutes = (int) Duration.between(startTime, endTime).toMinutes();

        // Validate rate if provided
        if (ratePerHour != null && ratePerHour.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La tarifa por hora debe ser mayor a cero");
        }

        ExtraAssignmentEntity assignment = new ExtraAssignmentEntity();
        assignment.setTeacher(teacher);
        assignment.setActivityType(activityType);
        assignment.setTitle(title);
        assignment.setAssignmentDate(assignmentDate);
        assignment.setStartTime(startTime);
        assignment.setEndTime(endTime);
        assignment.setDurationMinutes(durationMinutes);
        assignment.setRatePerHour(ratePerHour);
        assignment.setNotes(notes);

        return save(assignment);
    }

    @Transactional
    public ExtraAssignmentEntity updateAssignment(UUID uuid, String title,
                                                  LocalDate assignmentDate,
                                                  LocalTime startTime, LocalTime endTime,
                                                  BigDecimal ratePerHour, String notes) {
        ExtraAssignmentEntity assignment = findAssignmentOrThrow(uuid);

        // Validate times
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }

        // Recalculate duration
        int durationMinutes = (int) Duration.between(startTime, endTime).toMinutes();

        // Validate rate if provided
        if (ratePerHour != null && ratePerHour.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La tarifa por hora debe ser mayor a cero");
        }

        assignment.setTitle(title);
        assignment.setAssignmentDate(assignmentDate);
        assignment.setStartTime(startTime);
        assignment.setEndTime(endTime);
        assignment.setDurationMinutes(durationMinutes);
        assignment.setRatePerHour(ratePerHour);
        assignment.setNotes(notes);

        return save(assignment);
    }

    @Transactional
    public void deleteAssignment(UUID uuid) {
        ExtraAssignmentEntity assignment = findAssignmentOrThrow(uuid);
        // TODO: Check if this assignment is already included in a payroll
        deleteById(uuid);
    }

    /**
     * Calculate the total payment for an extra assignment
     */
    public BigDecimal calculatePayment(UUID assignmentUuid) {
        ExtraAssignmentEntity assignment = findAssignmentOrThrow(assignmentUuid);

        if (assignment.getRatePerHour() == null) {
            throw new IllegalStateException("No se puede calcular el pago sin una tarifa definida");
        }

        BigDecimal hours = BigDecimal.valueOf(assignment.getDurationMinutes())
                .divide(BigDecimal.valueOf(60), 4, BigDecimal.ROUND_HALF_UP);

        return assignment.getRatePerHour().multiply(hours);
    }

    /**
     * Get total hours for a teacher in a date range
     */
    public BigDecimal getTotalHoursForTeacher(UUID teacherUuid, LocalDate startDate, LocalDate endDate) {
        List<ExtraAssignmentEntity> assignments = extraAssignmentRepository
                .findByTeacherAndDateRange(teacherUuid, startDate, endDate);

        int totalMinutes = assignments.stream()
                .mapToInt(ExtraAssignmentEntity::getDurationMinutes)
                .sum();

        return BigDecimal.valueOf(totalMinutes)
                .divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Get total payment for a teacher in a date range
     */
    public BigDecimal getTotalPaymentForTeacher(UUID teacherUuid, LocalDate startDate, LocalDate endDate) {
        List<ExtraAssignmentEntity> assignments = extraAssignmentRepository
                .findByTeacherAndDateRange(teacherUuid, startDate, endDate);

        return assignments.stream()
                .filter(a -> a.getRatePerHour() != null)
                .map(a -> {
                    BigDecimal hours = BigDecimal.valueOf(a.getDurationMinutes())
                            .divide(BigDecimal.valueOf(60), 4, BigDecimal.ROUND_HALF_UP);
                    return a.getRatePerHour().multiply(hours);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Create bulk assignments for recurring activities
     */
    @Transactional
    public List<ExtraAssignmentEntity> createBulkAssignments(List<ExtraAssignmentEntity> assignments) {
        // Validate each assignment has required fields
        for (ExtraAssignmentEntity assignment : assignments) {
            if (assignment.getStartTime().isAfter(assignment.getEndTime())) {
                throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
            }
            // Calculate duration
            int duration = (int) Duration.between(assignment.getStartTime(), assignment.getEndTime()).toMinutes();
            assignment.setDurationMinutes(duration);
        }

        return saveAll(assignments);
    }
}
