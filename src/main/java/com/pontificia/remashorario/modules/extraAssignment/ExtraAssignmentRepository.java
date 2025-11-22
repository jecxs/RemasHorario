package com.pontificia.remashorario.modules.extraAssignment;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExtraAssignmentRepository extends BaseRepository<ExtraAssignmentEntity> {

    List<ExtraAssignmentEntity> findByTeacherUuid(UUID teacherUuid);

    List<ExtraAssignmentEntity> findByTeacherUuidAndAssignmentDate(UUID teacherUuid, LocalDate assignmentDate);

    @Query("SELECT ea FROM ExtraAssignmentEntity ea " +
            "WHERE ea.teacher.uuid = :teacherUuid " +
            "AND ea.assignmentDate BETWEEN :startDate AND :endDate")
    List<ExtraAssignmentEntity> findByTeacherAndDateRange(
            @Param("teacherUuid") UUID teacherUuid,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT ea FROM ExtraAssignmentEntity ea " +
            "WHERE ea.assignmentDate BETWEEN :startDate AND :endDate")
    List<ExtraAssignmentEntity> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<ExtraAssignmentEntity> findByActivityTypeUuid(UUID activityTypeUuid);

    @Query("SELECT ea FROM ExtraAssignmentEntity ea " +
            "WHERE ea.teacher.uuid = :teacherUuid " +
            "AND ea.activityType.uuid = :activityTypeUuid " +
            "AND ea.assignmentDate BETWEEN :startDate AND :endDate")
    List<ExtraAssignmentEntity> findByTeacherAndActivityTypeAndDateRange(
            @Param("teacherUuid") UUID teacherUuid,
            @Param("activityTypeUuid") UUID activityTypeUuid,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT ea FROM ExtraAssignmentEntity ea " +
            "LEFT JOIN FETCH ea.teacher " +
            "LEFT JOIN FETCH ea.activityType " +
            "WHERE ea.uuid = :uuid")
    Optional<ExtraAssignmentEntity> findByIdWithDetails(@Param("uuid") UUID uuid);
}
