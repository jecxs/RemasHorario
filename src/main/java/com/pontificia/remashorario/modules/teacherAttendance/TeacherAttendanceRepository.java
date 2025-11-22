package com.pontificia.remashorario.modules.teacherAttendance;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherAttendanceRepository extends BaseRepository<TeacherAttendanceEntity> {

    List<TeacherAttendanceEntity> findByTeacherUuid(UUID teacherUuid);

    List<TeacherAttendanceEntity> findByTeacherUuidAndAttendanceDate(UUID teacherUuid, LocalDate attendanceDate);

    List<TeacherAttendanceEntity> findByTeacherUuidAndAttendanceDateBetween(
            UUID teacherUuid,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("SELECT ta FROM TeacherAttendanceEntity ta " +
            "WHERE ta.teacher.uuid = :teacherUuid " +
            "AND ta.attendanceDate BETWEEN :startDate AND :endDate " +
            "AND ta.status = :status")
    List<TeacherAttendanceEntity> findByTeacherAndDateRangeAndStatus(
            @Param("teacherUuid") UUID teacherUuid,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") TeacherAttendanceEntity.AttendanceStatus status
    );

    @Query("SELECT ta FROM TeacherAttendanceEntity ta " +
            "WHERE ta.attendanceDate BETWEEN :startDate AND :endDate " +
            "AND ta.status = :status")
    List<TeacherAttendanceEntity> findByDateRangeAndStatus(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") TeacherAttendanceEntity.AttendanceStatus status
    );

    Optional<TeacherAttendanceEntity> findByClassSessionUuidAndAttendanceDate(
            UUID classSessionUuid,
            LocalDate attendanceDate
    );

    @Query("SELECT ta FROM TeacherAttendanceEntity ta " +
            "WHERE ta.teacher.uuid = :teacherUuid " +
            "AND ta.attendanceDate = :attendanceDate " +
            "AND ta.classSession.uuid = :classSessionUuid")
    Optional<TeacherAttendanceEntity> findByTeacherAndClassSessionAndDate(
            @Param("teacherUuid") UUID teacherUuid,
            @Param("classSessionUuid") UUID classSessionUuid,
            @Param("attendanceDate") LocalDate attendanceDate
    );

    @Query("SELECT ta FROM TeacherAttendanceEntity ta " +
            "LEFT JOIN FETCH ta.teacher " +
            "LEFT JOIN FETCH ta.classSession " +
            "LEFT JOIN FETCH ta.attendanceActivityType " +
            "WHERE ta.uuid = :uuid")
    Optional<TeacherAttendanceEntity> findByIdWithDetails(@Param("uuid") UUID uuid);

    @Query("SELECT ta FROM TeacherAttendanceEntity ta " +
            "WHERE ta.attendanceDate BETWEEN :startDate AND :endDate")
    List<TeacherAttendanceEntity> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<TeacherAttendanceEntity> findByIsHolidayTrue();

    @Query("SELECT ta FROM TeacherAttendanceEntity ta " +
            "WHERE ta.teacher.uuid = :teacherUuid " +
            "AND ta.status = 'PENDING' " +
            "ORDER BY ta.attendanceDate ASC")
    List<TeacherAttendanceEntity> findPendingAttendancesByTeacher(@Param("teacherUuid") UUID teacherUuid);
}
