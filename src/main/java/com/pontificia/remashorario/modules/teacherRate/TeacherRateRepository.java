package com.pontificia.remashorario.modules.teacherRate;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherRateRepository extends BaseRepository<TeacherRateEntity> {

    List<TeacherRateEntity> findByTeacherUuid(UUID teacherUuid);

    @Query("SELECT tr FROM TeacherRateEntity tr " +
            "WHERE tr.teacher.uuid = :teacherUuid " +
            "AND tr.activityType.uuid = :activityTypeUuid " +
            "AND (:date BETWEEN tr.effectiveFrom AND tr.effectiveTo " +
            "OR (tr.effectiveFrom <= :date AND tr.effectiveTo IS NULL))")
    Optional<TeacherRateEntity> findActiveRateByTeacherAndActivityType(
            @Param("teacherUuid") UUID teacherUuid,
            @Param("activityTypeUuid") UUID activityTypeUuid,
            @Param("date") LocalDate date
    );

    @Query("SELECT tr FROM TeacherRateEntity tr " +
            "WHERE tr.teacher.uuid = :teacherUuid " +
            "AND (:date BETWEEN tr.effectiveFrom AND tr.effectiveTo " +
            "OR (tr.effectiveFrom <= :date AND tr.effectiveTo IS NULL))")
    List<TeacherRateEntity> findActiveRatesByTeacher(
            @Param("teacherUuid") UUID teacherUuid,
            @Param("date") LocalDate date
    );

    List<TeacherRateEntity> findByActivityTypeUuid(UUID activityTypeUuid);

    @Query("SELECT tr FROM TeacherRateEntity tr " +
            "WHERE tr.teacher.uuid = :teacherUuid " +
            "AND tr.activityType.uuid = :activityTypeUuid")
    List<TeacherRateEntity> findByTeacherAndActivityType(
            @Param("teacherUuid") UUID teacherUuid,
            @Param("activityTypeUuid") UUID activityTypeUuid
    );

    @Query("SELECT tr FROM TeacherRateEntity tr " +
            "LEFT JOIN FETCH tr.teacher " +
            "LEFT JOIN FETCH tr.activityType " +
            "WHERE tr.uuid = :uuid")
    Optional<TeacherRateEntity> findByIdWithDetails(@Param("uuid") UUID uuid);
}
