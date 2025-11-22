package com.pontificia.remashorario.modules.payrollLine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pontificia.remashorario.modules.defaultRate.DefaultRateEntity;
import com.pontificia.remashorario.modules.defaultRate.DefaultRateRepository;
import com.pontificia.remashorario.modules.extraAssignment.ExtraAssignmentEntity;
import com.pontificia.remashorario.modules.extraAssignment.ExtraAssignmentRepository;
import com.pontificia.remashorario.modules.modalityRate.ModalityRateEntity;
import com.pontificia.remashorario.modules.modalityRate.ModalityRateRepository;
import com.pontificia.remashorario.modules.payrollPeriod.PayrollPeriodEntity;
import com.pontificia.remashorario.modules.payrollPeriod.PayrollPeriodService;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teacher.TeacherService;
import com.pontificia.remashorario.modules.teacherAttendance.TeacherAttendanceEntity;
import com.pontificia.remashorario.modules.teacherAttendance.TeacherAttendanceRepository;
import com.pontificia.remashorario.modules.teacherRate.TeacherRateEntity;
import com.pontificia.remashorario.modules.teacherRate.TeacherRateRepository;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating and managing teacher payroll
 * Handles complex calculations including:
 * - Regular class attendance
 * - Extra assignments (workshops, exams)
 * - Penalties for late arrival/early departure
 * - Rate resolution (teacher > modality > default)
 */
@Service
public class PayrollLineService extends BaseService<PayrollLineEntity> {

    private final PayrollLineRepository payrollLineRepository;
    private final PayrollPeriodService payrollPeriodService;
    private final TeacherService teacherService;
    private final TeacherAttendanceRepository attendanceRepository;
    private final ExtraAssignmentRepository extraAssignmentRepository;
    private final TeacherRateRepository teacherRateRepository;
    private final ModalityRateRepository modalityRateRepository;
    private final DefaultRateRepository defaultRateRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public PayrollLineService(PayrollLineRepository payrollLineRepository,
                             PayrollPeriodService payrollPeriodService,
                             TeacherService teacherService,
                             TeacherAttendanceRepository attendanceRepository,
                             ExtraAssignmentRepository extraAssignmentRepository,
                             TeacherRateRepository teacherRateRepository,
                             ModalityRateRepository modalityRateRepository,
                             DefaultRateRepository defaultRateRepository,
                             ObjectMapper objectMapper) {
        super(payrollLineRepository);
        this.payrollLineRepository = payrollLineRepository;
        this.payrollPeriodService = payrollPeriodService;
        this.teacherService = teacherService;
        this.attendanceRepository = attendanceRepository;
        this.extraAssignmentRepository = extraAssignmentRepository;
        this.teacherRateRepository = teacherRateRepository;
        this.modalityRateRepository = modalityRateRepository;
        this.defaultRateRepository = defaultRateRepository;
        this.objectMapper = objectMapper;
    }

    public List<PayrollLineEntity> getAllPayrollLines() {
        return findAll();
    }

    public PayrollLineEntity getPayrollLineById(UUID uuid) {
        return findPayrollLineOrThrow(uuid);
    }

    public PayrollLineEntity getPayrollLineByIdWithDetails(UUID uuid) {
        return payrollLineRepository.findByIdWithDetails(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Línea de nómina no encontrada con ID: " + uuid));
    }

    public List<PayrollLineEntity> getPayrollLinesByPeriod(UUID payrollPeriodUuid) {
        return payrollLineRepository.findByPayrollPeriodWithDetails(payrollPeriodUuid);
    }

    public List<PayrollLineEntity> getPayrollLinesByTeacher(UUID teacherUuid) {
        return payrollLineRepository.findByTeacherOrderByPeriodDesc(teacherUuid);
    }

    public PayrollLineEntity getPayrollLineByPeriodAndTeacher(UUID payrollPeriodUuid, UUID teacherUuid) {
        return payrollLineRepository.findByPayrollPeriodAndTeacher(payrollPeriodUuid, teacherUuid)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró línea de nómina para el período y docente especificados"));
    }

    public PayrollLineEntity findPayrollLineOrThrow(UUID uuid) {
        return findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Línea de nómina no encontrada con ID: " + uuid));
    }

    /**
     * Calculate payroll for a specific teacher in a period
     * This is the main calculation method
     */
    @Transactional
    public PayrollLineEntity calculatePayrollForTeacher(UUID payrollPeriodUuid, UUID teacherUuid) {
        PayrollPeriodEntity period = payrollPeriodService.findPeriodOrThrow(payrollPeriodUuid);
        TeacherEntity teacher = teacherService.findTeacherOrThrow(teacherUuid);

        // Check if already exists and delete to recalculate
        payrollLineRepository.findByPayrollPeriodAndTeacher(payrollPeriodUuid, teacherUuid)
                .ifPresent(existing -> deleteById(existing.getUuid()));

        // Get all approved attendances for the period
        List<TeacherAttendanceEntity> attendances = attendanceRepository
                .findByTeacherUuidAndAttendanceDateBetween(
                        teacherUuid, period.getStartDate(), period.getEndDate())
                .stream()
                .filter(a -> a.getStatus() == TeacherAttendanceEntity.AttendanceStatus.APPROVED ||
                           a.getStatus() == TeacherAttendanceEntity.AttendanceStatus.OVERRIDDEN ||
                           a.getStatus() == TeacherAttendanceEntity.AttendanceStatus.HOLIDAY)
                .collect(Collectors.toList());

        // Get all extra assignments for the period
        List<ExtraAssignmentEntity> extraAssignments = extraAssignmentRepository
                .findByTeacherAndDateRange(teacherUuid, period.getStartDate(), period.getEndDate());

        // Calculate totals
        PayrollCalculation calculation = calculatePayroll(teacher, attendances, extraAssignments, period);

        // Create payroll line
        PayrollLineEntity payrollLine = new PayrollLineEntity();
        payrollLine.setPayrollPeriod(period);
        payrollLine.setTeacher(teacher);
        payrollLine.setTotalHoursWorked(calculation.totalHoursWorked);
        payrollLine.setTotalHoursScheduled(calculation.totalHoursScheduled);
        payrollLine.setGrossAmount(calculation.grossAmount);
        payrollLine.setTotalPenalties(calculation.totalPenalties);
        payrollLine.setNetAmount(calculation.netAmount);
        payrollLine.setDetails(calculation.detailsJson);
        payrollLine.setGeneratedAt(LocalDateTime.now());

        return save(payrollLine);
    }

    /**
     * Calculate payroll for all teachers in a period
     */
    @Transactional
    public List<PayrollLineEntity> calculatePayrollForAllTeachers(UUID payrollPeriodUuid) {
        PayrollPeriodEntity period = payrollPeriodService.findPeriodOrThrow(payrollPeriodUuid);

        // Get all teachers who have attendances or extra assignments in this period
        Set<UUID> teacherUuids = new HashSet<>();

        attendanceRepository.findByDateRange(period.getStartDate(), period.getEndDate())
                .forEach(a -> teacherUuids.add(a.getTeacher().getUuid()));

        extraAssignmentRepository.findByDateRange(period.getStartDate(), period.getEndDate())
                .forEach(ea -> teacherUuids.add(ea.getTeacher().getUuid()));

        List<PayrollLineEntity> payrollLines = new ArrayList<>();
        for (UUID teacherUuid : teacherUuids) {
            try {
                PayrollLineEntity line = calculatePayrollForTeacher(payrollPeriodUuid, teacherUuid);
                payrollLines.add(line);
            } catch (Exception e) {
                // Log error and continue with next teacher
                System.err.println("Error calculating payroll for teacher " + teacherUuid + ": " + e.getMessage());
            }
        }

        // Mark period as calculated
        payrollPeriodService.markAsCalculated(payrollPeriodUuid);

        return payrollLines;
    }

    /**
     * Core payroll calculation logic
     */
    private PayrollCalculation calculatePayroll(TeacherEntity teacher,
                                               List<TeacherAttendanceEntity> attendances,
                                               List<ExtraAssignmentEntity> extraAssignments,
                                               PayrollPeriodEntity period) {
        PayrollCalculation calc = new PayrollCalculation();
        List<PayrollDetailItem> details = new ArrayList<>();

        // Calculate from attendances
        for (TeacherAttendanceEntity attendance : attendances) {
            PayrollDetailItem item = calculateAttendancePayment(teacher, attendance, period);
            details.add(item);

            calc.totalHoursWorked = calc.totalHoursWorked.add(item.hoursWorked);
            calc.totalHoursScheduled = calc.totalHoursScheduled.add(item.hoursScheduled);
            calc.grossAmount = calc.grossAmount.add(item.grossAmount);
            calc.totalPenalties = calc.totalPenalties.add(item.penaltyAmount);
        }

        // Calculate from extra assignments
        for (ExtraAssignmentEntity assignment : extraAssignments) {
            PayrollDetailItem item = calculateExtraAssignmentPayment(teacher, assignment, period);
            details.add(item);

            calc.totalHoursWorked = calc.totalHoursWorked.add(item.hoursWorked);
            calc.totalHoursScheduled = calc.totalHoursScheduled.add(item.hoursWorked); // Same for extras
            calc.grossAmount = calc.grossAmount.add(item.grossAmount);
        }

        // Calculate net amount
        calc.netAmount = calc.grossAmount.subtract(calc.totalPenalties);

        // Convert details to JSON
        try {
            calc.detailsJson = objectMapper.writeValueAsString(details);
        } catch (Exception e) {
            calc.detailsJson = "[]";
        }

        return calc;
    }

    /**
     * Calculate payment for a single attendance
     */
    private PayrollDetailItem calculateAttendancePayment(TeacherEntity teacher,
                                                         TeacherAttendanceEntity attendance,
                                                         PayrollPeriodEntity period) {
        PayrollDetailItem item = new PayrollDetailItem();
        item.type = "ATTENDANCE";
        item.date = attendance.getAttendanceDate().toString();
        item.activityType = attendance.getAttendanceActivityType().getName();

        // Get applicable rate
        BigDecimal ratePerHour = resolveRateForTeacher(
                teacher.getUuid(),
                attendance.getAttendanceActivityType().getUuid(),
                null, // We'd need modality from class session
                attendance.getAttendanceDate()
        );
        item.ratePerHour = ratePerHour;

        // Calculate hours worked
        BigDecimal minutesWorked = BigDecimal.valueOf(
                attendance.getActualDurationMinutes() != null ? attendance.getActualDurationMinutes() : 0
        );
        item.hoursWorked = minutesWorked.divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);

        // Calculate hours scheduled
        BigDecimal minutesScheduled = BigDecimal.valueOf(
                attendance.getScheduledDurationMinutes() != null ? attendance.getScheduledDurationMinutes() : 0
        );
        item.hoursScheduled = minutesScheduled.divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);

        // Calculate gross amount (based on actual hours worked)
        item.grossAmount = item.hoursWorked.multiply(ratePerHour).setScale(2, RoundingMode.HALF_UP);

        // Calculate penalties
        BigDecimal ratePerMinute = ratePerHour.divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        int totalPenaltyMinutes = attendance.getLateMinutes() + attendance.getEarlyDepartureMinutes();
        item.penaltyMinutes = totalPenaltyMinutes;
        item.penaltyAmount = ratePerMinute.multiply(BigDecimal.valueOf(totalPenaltyMinutes))
                .setScale(2, RoundingMode.HALF_UP);

        item.lateMinutes = attendance.getLateMinutes();
        item.earlyDepartureMinutes = attendance.getEarlyDepartureMinutes();

        return item;
    }

    /**
     * Calculate payment for an extra assignment
     */
    private PayrollDetailItem calculateExtraAssignmentPayment(TeacherEntity teacher,
                                                             ExtraAssignmentEntity assignment,
                                                             PayrollPeriodEntity period) {
        PayrollDetailItem item = new PayrollDetailItem();
        item.type = "EXTRA_ASSIGNMENT";
        item.date = assignment.getAssignmentDate().toString();
        item.activityType = assignment.getActivityType().getName();
        item.description = assignment.getTitle();

        // Use assignment's specific rate if set, otherwise resolve
        BigDecimal ratePerHour;
        if (assignment.getRatePerHour() != null) {
            ratePerHour = assignment.getRatePerHour();
        } else {
            ratePerHour = resolveRateForTeacher(
                    teacher.getUuid(),
                    assignment.getActivityType().getUuid(),
                    null,
                    assignment.getAssignmentDate()
            );
        }
        item.ratePerHour = ratePerHour;

        // Calculate hours
        BigDecimal minutes = BigDecimal.valueOf(assignment.getDurationMinutes());
        item.hoursWorked = minutes.divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        item.hoursScheduled = item.hoursWorked;

        // Calculate amount (no penalties for extra assignments)
        item.grossAmount = item.hoursWorked.multiply(ratePerHour).setScale(2, RoundingMode.HALF_UP);
        item.penaltyAmount = BigDecimal.ZERO;
        item.penaltyMinutes = 0;

        return item;
    }

    /**
     * Resolve the applicable rate for a teacher
     * Priority: Teacher Rate > Modality Rate > Default Rate
     */
    private BigDecimal resolveRateForTeacher(UUID teacherUuid, UUID activityTypeUuid,
                                            UUID modalityUuid, java.time.LocalDate date) {
        // Try teacher-specific rate first
        Optional<TeacherRateEntity> teacherRate = teacherRateRepository
                .findActiveRateByTeacherAndActivityType(teacherUuid, activityTypeUuid, date);
        if (teacherRate.isPresent()) {
            return teacherRate.get().getRatePerHour();
        }

        // Try modality rate if modality is provided
        if (modalityUuid != null) {
            Optional<ModalityRateEntity> modalityRate = modalityRateRepository
                    .findActiveRateByModalityAndActivityType(modalityUuid, activityTypeUuid, date);
            if (modalityRate.isPresent()) {
                return modalityRate.get().getRatePerHour();
            }
        }

        // Fall back to default rate
        Optional<DefaultRateEntity> defaultRate = defaultRateRepository
                .findActiveRateByActivityType(activityTypeUuid, date);
        if (defaultRate.isPresent()) {
            return defaultRate.get().getRatePerHour();
        }

        throw new IllegalStateException(
                "No se encontró tarifa aplicable para el tipo de actividad en la fecha: " + date);
    }

    /**
     * Get total net amount for a period
     */
    public BigDecimal getTotalNetAmountByPeriod(UUID payrollPeriodUuid) {
        return payrollLineRepository.calculateTotalNetAmountByPeriod(payrollPeriodUuid);
    }

    /**
     * Get total gross amount for a period
     */
    public BigDecimal getTotalGrossAmountByPeriod(UUID payrollPeriodUuid) {
        return payrollLineRepository.calculateTotalGrossAmountByPeriod(payrollPeriodUuid);
    }

    /**
     * Get total penalties for a period
     */
    public BigDecimal getTotalPenaltiesByPeriod(UUID payrollPeriodUuid) {
        return payrollLineRepository.calculateTotalPenaltiesByPeriod(payrollPeriodUuid);
    }

    /**
     * Get teacher count for a period
     */
    public Long getTeacherCountByPeriod(UUID payrollPeriodUuid) {
        return payrollLineRepository.countByPayrollPeriod(payrollPeriodUuid);
    }

    /**
     * Delete a payroll line (only if period is in DRAFT status)
     */
    @Transactional
    public void deletePayrollLine(UUID uuid) {
        PayrollLineEntity payrollLine = findPayrollLineOrThrow(uuid);

        if (payrollLine.getPayrollPeriod().getStatus() != PayrollPeriodEntity.PayrollStatus.DRAFT) {
            throw new IllegalStateException("Solo se pueden eliminar líneas de nómina de períodos en estado BORRADOR");
        }

        deleteById(uuid);
    }

    /**
     * Recalculate all payroll lines for a period
     */
    @Transactional
    public List<PayrollLineEntity> recalculatePayrollForPeriod(UUID payrollPeriodUuid) {
        PayrollPeriodEntity period = payrollPeriodService.findPeriodOrThrow(payrollPeriodUuid);

        // Only allow recalculation if in DRAFT or CALCULATED status
        if (period.getStatus() != PayrollPeriodEntity.PayrollStatus.DRAFT &&
            period.getStatus() != PayrollPeriodEntity.PayrollStatus.CALCULATED) {
            throw new IllegalStateException("Solo se pueden recalcular períodos en estado BORRADOR o CALCULADO");
        }

        // Revert to draft first
        payrollPeriodService.revertToDraft(payrollPeriodUuid);

        // Delete existing lines
        List<PayrollLineEntity> existingLines = payrollLineRepository.findByPayrollPeriodUuid(payrollPeriodUuid);
        existingLines.forEach(line -> deleteById(line.getUuid()));

        // Recalculate
        return calculatePayrollForAllTeachers(payrollPeriodUuid);
    }

    /**
     * Inner classes for calculation
     */
    private static class PayrollCalculation {
        BigDecimal totalHoursWorked = BigDecimal.ZERO;
        BigDecimal totalHoursScheduled = BigDecimal.ZERO;
        BigDecimal grossAmount = BigDecimal.ZERO;
        BigDecimal totalPenalties = BigDecimal.ZERO;
        BigDecimal netAmount = BigDecimal.ZERO;
        String detailsJson;
    }

    private static class PayrollDetailItem {
        String type;
        String date;
        String activityType;
        String description;
        BigDecimal ratePerHour;
        BigDecimal hoursWorked;
        BigDecimal hoursScheduled;
        BigDecimal grossAmount;
        int penaltyMinutes;
        BigDecimal penaltyAmount;
        int lateMinutes;
        int earlyDepartureMinutes;
    }
}
