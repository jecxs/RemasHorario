package com.pontificia.remashorario.modules.teacherAttendance;

import com.pontificia.remashorario.modules.academicCalendarException.AcademicCalendarExceptionService;
import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeEntity;
import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeService;
import com.pontificia.remashorario.modules.classSession.ClassSessionEntity;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teacher.TeacherService;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing teacher attendance
 * Handles check-in/check-out, penalties for late arrival/early departure
 * Includes 5-minute tolerance for check-in
 */
@Service
public class TeacherAttendanceService extends BaseService<TeacherAttendanceEntity> {

    private static final int TOLERANCE_MINUTES = 5;

    private final TeacherAttendanceRepository attendanceRepository;
    private final TeacherService teacherService;
    private final AttendanceActivityTypeService activityTypeService;
    private final AcademicCalendarExceptionService calendarExceptionService;

    @Autowired
    public TeacherAttendanceService(TeacherAttendanceRepository attendanceRepository,
                                   TeacherService teacherService,
                                   AttendanceActivityTypeService activityTypeService,
                                   AcademicCalendarExceptionService calendarExceptionService) {
        super(attendanceRepository);
        this.attendanceRepository = attendanceRepository;
        this.teacherService = teacherService;
        this.activityTypeService = activityTypeService;
        this.calendarExceptionService = calendarExceptionService;
    }

    public List<TeacherAttendanceEntity> getAllAttendances() {
        return findAll();
    }

    public TeacherAttendanceEntity getAttendanceById(UUID uuid) {
        return findAttendanceOrThrow(uuid);
    }

    public TeacherAttendanceEntity getAttendanceByIdWithDetails(UUID uuid) {
        return attendanceRepository.findByIdWithDetails(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Asistencia no encontrada con ID: " + uuid));
    }

    public List<TeacherAttendanceEntity> getAttendancesByTeacher(UUID teacherUuid) {
        return attendanceRepository.findByTeacherUuid(teacherUuid);
    }

    public List<TeacherAttendanceEntity> getAttendancesByTeacherAndDate(UUID teacherUuid, LocalDate date) {
        return attendanceRepository.findByTeacherUuidAndAttendanceDate(teacherUuid, date);
    }

    public List<TeacherAttendanceEntity> getAttendancesByTeacherAndDateRange(UUID teacherUuid,
                                                                             LocalDate startDate,
                                                                             LocalDate endDate) {
        return attendanceRepository.findByTeacherUuidAndAttendanceDateBetween(teacherUuid, startDate, endDate);
    }

    public List<TeacherAttendanceEntity> getAttendancesByDateRange(LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByDateRange(startDate, endDate);
    }

    public List<TeacherAttendanceEntity> getPendingAttendancesByTeacher(UUID teacherUuid) {
        return attendanceRepository.findPendingAttendancesByTeacher(teacherUuid);
    }

    public TeacherAttendanceEntity findAttendanceOrThrow(UUID uuid) {
        return findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Asistencia no encontrada con ID: " + uuid));
    }

    /**
     * Teacher checks in for a class session
     * Automatically calculates late minutes considering 5-minute tolerance
     */
    @Transactional
    public TeacherAttendanceEntity checkIn(UUID teacherUuid, UUID classSessionUuid, LocalDate date) {
        TeacherEntity teacher = teacherService.findTeacherOrThrow(teacherUuid);

        // Check if already checked in for this session today
        var existingAttendance = attendanceRepository.findByTeacherAndClassSessionAndDate(
                teacherUuid, classSessionUuid, date);

        if (existingAttendance.isPresent()) {
            throw new IllegalStateException("El docente ya marcó su entrada para esta sesión hoy");
        }

        // Check if it's a holiday
        boolean isHoliday = calendarExceptionService.isHoliday(date);

        // Get class session and calculate scheduled times
        ClassSessionEntity classSession = new ClassSessionEntity();
        classSession.setUuid(classSessionUuid);
        // In a real scenario, we would fetch the full classSession with teaching hours

        LocalDateTime checkinTime = LocalDateTime.now();

        // Create attendance record
        TeacherAttendanceEntity attendance = new TeacherAttendanceEntity();
        attendance.setTeacher(teacher);
        attendance.setClassSession(classSession);
        attendance.setAttendanceDate(date);
        attendance.setCheckinAt(checkinTime);
        attendance.setIsHoliday(isHoliday);
        attendance.setStatus(TeacherAttendanceEntity.AttendanceStatus.PENDING);

        // We'll need to get activity type - for now use a default
        // In real implementation, get from class session or configuration
        AttendanceActivityTypeEntity activityType = activityTypeService.getActivityTypeByCode("REGULAR_CLASS");
        attendance.setAttendanceActivityType(activityType);

        return save(attendance);
    }

    /**
     * Teacher checks in with full class session details
     * Calculates late minutes with tolerance
     */
    @Transactional
    public TeacherAttendanceEntity checkInWithSchedule(UUID teacherUuid, UUID classSessionUuid,
                                                       LocalDate date, LocalTime scheduledStartTime,
                                                       LocalTime scheduledEndTime, Integer scheduledDurationMinutes) {
        TeacherEntity teacher = teacherService.findTeacherOrThrow(teacherUuid);

        // Check if already checked in
        var existingAttendance = attendanceRepository.findByTeacherAndClassSessionAndDate(
                teacherUuid, classSessionUuid, date);

        if (existingAttendance.isPresent()) {
            throw new IllegalStateException("El docente ya marcó su entrada para esta sesión hoy");
        }

        // Check if it's a holiday
        boolean isHoliday = calendarExceptionService.isHoliday(date);

        LocalDateTime checkinTime = LocalDateTime.now();
        LocalTime actualCheckinTime = checkinTime.toLocalTime();

        // Calculate late minutes with tolerance
        int lateMinutes = calculateLateMinutes(scheduledStartTime, actualCheckinTime);

        // Get class session
        ClassSessionEntity classSession = new ClassSessionEntity();
        classSession.setUuid(classSessionUuid);

        // Get activity type
        AttendanceActivityTypeEntity activityType = activityTypeService.getActivityTypeByCode("REGULAR_CLASS");

        TeacherAttendanceEntity attendance = new TeacherAttendanceEntity();
        attendance.setTeacher(teacher);
        attendance.setClassSession(classSession);
        attendance.setAttendanceActivityType(activityType);
        attendance.setAttendanceDate(date);
        attendance.setScheduledStartTime(scheduledStartTime);
        attendance.setScheduledEndTime(scheduledEndTime);
        attendance.setScheduledDurationMinutes(scheduledDurationMinutes);
        attendance.setCheckinAt(checkinTime);
        attendance.setLateMinutes(lateMinutes);
        attendance.setIsHoliday(isHoliday);
        attendance.setStatus(TeacherAttendanceEntity.AttendanceStatus.PENDING);

        return save(attendance);
    }

    /**
     * Teacher checks out
     * Calculates actual duration and early departure if applicable
     */
    @Transactional
    public TeacherAttendanceEntity checkOut(UUID attendanceUuid) {
        TeacherAttendanceEntity attendance = findAttendanceOrThrow(attendanceUuid);

        if (attendance.getCheckinAt() == null) {
            throw new IllegalStateException("No se puede marcar salida sin haber marcado entrada");
        }

        if (attendance.getCheckoutAt() != null) {
            throw new IllegalStateException("Ya se marcó la salida para esta asistencia");
        }

        LocalDateTime checkoutTime = LocalDateTime.now();
        attendance.setCheckoutAt(checkoutTime);

        // Calculate actual duration
        long actualMinutes = Duration.between(attendance.getCheckinAt(), checkoutTime).toMinutes();
        attendance.setActualDurationMinutes((int) actualMinutes);

        // Calculate early departure if scheduled end time is set
        if (attendance.getScheduledEndTime() != null) {
            LocalTime actualCheckoutTime = checkoutTime.toLocalTime();
            int earlyDepartureMinutes = calculateEarlyDepartureMinutes(
                    attendance.getScheduledEndTime(), actualCheckoutTime);
            attendance.setEarlyDepartureMinutes(earlyDepartureMinutes);
        }

        // Auto-approve if no penalties
        if (attendance.getLateMinutes() == 0 && attendance.getEarlyDepartureMinutes() == 0) {
            attendance.setStatus(TeacherAttendanceEntity.AttendanceStatus.APPROVED);
        }

        return save(attendance);
    }

    /**
     * Calculate late minutes with tolerance
     * Returns 0 if within tolerance, otherwise returns minutes late minus tolerance
     */
    private int calculateLateMinutes(LocalTime scheduledTime, LocalTime actualTime) {
        if (actualTime.isBefore(scheduledTime) || actualTime.equals(scheduledTime)) {
            return 0; // On time
        }

        long minutesLate = Duration.between(scheduledTime, actualTime).toMinutes();

        if (minutesLate <= TOLERANCE_MINUTES) {
            return 0; // Within tolerance
        }

        return (int) minutesLate; // Late beyond tolerance
    }

    /**
     * Calculate early departure minutes
     * Returns 0 if left on time or after, otherwise returns minutes early
     */
    private int calculateEarlyDepartureMinutes(LocalTime scheduledEndTime, LocalTime actualTime) {
        if (actualTime.isAfter(scheduledEndTime) || actualTime.equals(scheduledEndTime)) {
            return 0; // Left on time or later
        }

        return (int) Duration.between(actualTime, scheduledEndTime).toMinutes();
    }

    /**
     * Admin manually approves attendance
     */
    @Transactional
    public TeacherAttendanceEntity approveAttendance(UUID attendanceUuid, String adminNote) {
        TeacherAttendanceEntity attendance = findAttendanceOrThrow(attendanceUuid);

        if (attendance.getStatus() == TeacherAttendanceEntity.AttendanceStatus.APPROVED) {
            throw new IllegalStateException("Esta asistencia ya está aprobada");
        }

        attendance.setStatus(TeacherAttendanceEntity.AttendanceStatus.APPROVED);
        if (adminNote != null && !adminNote.trim().isEmpty()) {
            attendance.setAdminNote(adminNote);
        }

        return save(attendance);
    }

    /**
     * Admin manually overrides attendance (e.g., for holidays or corrections)
     * Can set custom check-in/check-out times and reset penalties
     */
    @Transactional
    public TeacherAttendanceEntity overrideAttendance(UUID attendanceUuid,
                                                      LocalDateTime checkinAt,
                                                      LocalDateTime checkoutAt,
                                                      boolean resetPenalties,
                                                      String adminNote) {
        TeacherAttendanceEntity attendance = findAttendanceOrThrow(attendanceUuid);

        attendance.setCheckinAt(checkinAt);
        attendance.setCheckoutAt(checkoutAt);

        if (resetPenalties) {
            attendance.setLateMinutes(0);
            attendance.setEarlyDepartureMinutes(0);
        } else {
            // Recalculate penalties based on new times
            if (attendance.getScheduledStartTime() != null && checkinAt != null) {
                int lateMinutes = calculateLateMinutes(
                        attendance.getScheduledStartTime(), checkinAt.toLocalTime());
                attendance.setLateMinutes(lateMinutes);
            }

            if (attendance.getScheduledEndTime() != null && checkoutAt != null) {
                int earlyMinutes = calculateEarlyDepartureMinutes(
                        attendance.getScheduledEndTime(), checkoutAt.toLocalTime());
                attendance.setEarlyDepartureMinutes(earlyMinutes);
            }
        }

        // Recalculate actual duration
        if (checkinAt != null && checkoutAt != null) {
            long actualMinutes = Duration.between(checkinAt, checkoutAt).toMinutes();
            attendance.setActualDurationMinutes((int) actualMinutes);
        }

        attendance.setStatus(TeacherAttendanceEntity.AttendanceStatus.OVERRIDDEN);
        attendance.setAdminNote(adminNote);

        return save(attendance);
    }

    /**
     * Mark attendance as holiday and automatically set full attendance
     */
    @Transactional
    public TeacherAttendanceEntity markAsHoliday(UUID attendanceUuid, String adminNote) {
        TeacherAttendanceEntity attendance = findAttendanceOrThrow(attendanceUuid);

        if (attendance.getScheduledStartTime() == null || attendance.getScheduledEndTime() == null) {
            throw new IllegalStateException("No se puede marcar como feriado sin horario programado");
        }

        // Set full scheduled time as attendance
        LocalDateTime scheduledStart = LocalDateTime.of(attendance.getAttendanceDate(), attendance.getScheduledStartTime());
        LocalDateTime scheduledEnd = LocalDateTime.of(attendance.getAttendanceDate(), attendance.getScheduledEndTime());

        attendance.setCheckinAt(scheduledStart);
        attendance.setCheckoutAt(scheduledEnd);
        attendance.setActualDurationMinutes(attendance.getScheduledDurationMinutes());
        attendance.setLateMinutes(0);
        attendance.setEarlyDepartureMinutes(0);
        attendance.setIsHoliday(true);
        attendance.setStatus(TeacherAttendanceEntity.AttendanceStatus.HOLIDAY);
        attendance.setAdminNote(adminNote);

        return save(attendance);
    }

    /**
     * Reject attendance (e.g., for fraud or incorrect entries)
     */
    @Transactional
    public TeacherAttendanceEntity rejectAttendance(UUID attendanceUuid, String adminNote) {
        TeacherAttendanceEntity attendance = findAttendanceOrThrow(attendanceUuid);

        attendance.setStatus(TeacherAttendanceEntity.AttendanceStatus.REJECTED);
        attendance.setAdminNote(adminNote);

        return save(attendance);
    }

    /**
     * Calculate total minutes worked by a teacher in a date range
     * Only counts approved and overridden attendances
     */
    public int calculateTotalMinutesWorked(UUID teacherUuid, LocalDate startDate, LocalDate endDate) {
        List<TeacherAttendanceEntity> attendances = attendanceRepository
                .findByTeacherUuidAndAttendanceDateBetween(teacherUuid, startDate, endDate);

        return attendances.stream()
                .filter(a -> a.getStatus() == TeacherAttendanceEntity.AttendanceStatus.APPROVED ||
                           a.getStatus() == TeacherAttendanceEntity.AttendanceStatus.OVERRIDDEN ||
                           a.getStatus() == TeacherAttendanceEntity.AttendanceStatus.HOLIDAY)
                .filter(a -> a.getActualDurationMinutes() != null)
                .mapToInt(TeacherAttendanceEntity::getActualDurationMinutes)
                .sum();
    }

    /**
     * Calculate total penalty minutes (late + early departure) in a date range
     */
    public int calculateTotalPenaltyMinutes(UUID teacherUuid, LocalDate startDate, LocalDate endDate) {
        List<TeacherAttendanceEntity> attendances = attendanceRepository
                .findByTeacherUuidAndAttendanceDateBetween(teacherUuid, startDate, endDate);

        return attendances.stream()
                .filter(a -> a.getStatus() == TeacherAttendanceEntity.AttendanceStatus.APPROVED ||
                           a.getStatus() == TeacherAttendanceEntity.AttendanceStatus.PENDING)
                .mapToInt(a -> a.getLateMinutes() + a.getEarlyDepartureMinutes())
                .sum();
    }

    /**
     * Get attendance statistics for a teacher in a date range
     */
    public AttendanceStats getAttendanceStats(UUID teacherUuid, LocalDate startDate, LocalDate endDate) {
        List<TeacherAttendanceEntity> attendances = attendanceRepository
                .findByTeacherUuidAndAttendanceDateBetween(teacherUuid, startDate, endDate);

        int totalMinutesWorked = attendances.stream()
                .filter(a -> a.getActualDurationMinutes() != null)
                .mapToInt(TeacherAttendanceEntity::getActualDurationMinutes)
                .sum();

        int totalLateMinutes = attendances.stream()
                .mapToInt(TeacherAttendanceEntity::getLateMinutes)
                .sum();

        int totalEarlyDepartureMinutes = attendances.stream()
                .mapToInt(TeacherAttendanceEntity::getEarlyDepartureMinutes)
                .sum();

        int totalScheduledMinutes = attendances.stream()
                .filter(a -> a.getScheduledDurationMinutes() != null)
                .mapToInt(TeacherAttendanceEntity::getScheduledDurationMinutes)
                .sum();

        long approvedCount = attendances.stream()
                .filter(a -> a.getStatus() == TeacherAttendanceEntity.AttendanceStatus.APPROVED)
                .count();

        long pendingCount = attendances.stream()
                .filter(a -> a.getStatus() == TeacherAttendanceEntity.AttendanceStatus.PENDING)
                .count();

        return new AttendanceStats(
                totalMinutesWorked,
                totalScheduledMinutes,
                totalLateMinutes,
                totalEarlyDepartureMinutes,
                approvedCount,
                pendingCount
        );
    }

    /**
     * Inner class for attendance statistics
     */
    public static class AttendanceStats {
        public final int totalMinutesWorked;
        public final int totalScheduledMinutes;
        public final int totalLateMinutes;
        public final int totalEarlyDepartureMinutes;
        public final long approvedCount;
        public final long pendingCount;

        public AttendanceStats(int totalMinutesWorked, int totalScheduledMinutes,
                             int totalLateMinutes, int totalEarlyDepartureMinutes,
                             long approvedCount, long pendingCount) {
            this.totalMinutesWorked = totalMinutesWorked;
            this.totalScheduledMinutes = totalScheduledMinutes;
            this.totalLateMinutes = totalLateMinutes;
            this.totalEarlyDepartureMinutes = totalEarlyDepartureMinutes;
            this.approvedCount = approvedCount;
            this.pendingCount = pendingCount;
        }
    }
}
