package com.pontificia.remashorario.modules.classSession;

import com.pontificia.remashorario.modules.learningSpace.LearningSpaceEntity;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClassSessionRepository extends BaseRepository<ClassSessionEntity> {

    @Query(value = """
    SELECT cs.* FROM class_session cs 
    WHERE cs.day_of_week = :dayOfWeek 
    AND (
        (:teacherUuid IS NULL OR cs.teacher_id = :teacherUuid) AND
        (:spaceUuid IS NULL OR cs.learning_space_id = :spaceUuid) AND
        (:groupUuid IS NULL OR cs.student_group_id = :groupUuid)
    )
    AND EXISTS (
        SELECT 1 FROM class_session_teaching_hour csth 
        JOIN teaching_hour th ON th.uuid = csth.teaching_hour_id 
        WHERE csth.class_session_id = cs.uuid 
        AND th.start_time < CAST(:endTime AS TIME) 
        AND th.end_time > CAST(:startTime AS TIME)
    )
    """, nativeQuery = true)
    List<ClassSessionEntity> findConflicts(
            @Param("teacherUuid") UUID teacherUuid,
            @Param("spaceUuid") UUID spaceUuid,
            @Param("groupUuid") UUID groupUuid,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    // ✅ SOLUCIÓN: Usar query nativa para SQL Server
    @Query(value = """
        SELECT cs.* FROM class_session cs 
        WHERE cs.learning_space_id = :spaceId 
        AND cs.day_of_week = :dayOfWeek 
        AND EXISTS (
            SELECT 1 FROM class_session_teaching_hour csth 
            JOIN teaching_hour th ON th.uuid = csth.teaching_hour_id 
            WHERE csth.class_session_id = cs.uuid 
            AND th.start_time < CAST(:endTime AS TIME) 
            AND th.end_time > CAST(:startTime AS TIME)
        )
        """, nativeQuery = true)
    List<ClassSessionEntity> findByLearningSpaceAndDayOfWeekAndTimeSlotOverlap(
            @Param("spaceId") UUID spaceId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);


    @Query(value = """
    SELECT DISTINCT cs.* FROM class_session cs 
    INNER JOIN class_session_teaching_hour csth ON cs.uuid = csth.class_session_id
    WHERE cs.learning_space_id = :spaceId 
    AND UPPER(cs.day_of_week) = UPPER(:dayOfWeek)
    AND csth.teaching_hour_id IN :teachingHourUuids
    """, nativeQuery = true)
    List<ClassSessionEntity> findByLearningSpaceAndDayOfWeekAndSpecificHours(
            @Param("spaceId") UUID spaceId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("teachingHourUuids") List<String> teachingHourUuids);


    List<ClassSessionEntity> findByDayOfWeekAndTeachingHoursContaining(DayOfWeek dayOfWeek, TeachingHourEntity teachingHour);


    // ✅ NUEVOS MÉTODOS con filtro de periodo
    @Query("SELECT cs FROM ClassSessionEntity cs WHERE cs.studentGroup.uuid = :studentGroupUuid AND cs.period.uuid = :periodUuid")
    List<ClassSessionEntity> findByStudentGroupUuidAndPeriod(@Param("studentGroupUuid") UUID studentGroupUuid, @Param("periodUuid") UUID periodUuid);

    @Query("SELECT cs FROM ClassSessionEntity cs WHERE cs.teacher.uuid = :teacherUuid AND cs.period.uuid = :periodUuid")
    List<ClassSessionEntity> findByTeacherUuidAndPeriod(@Param("teacherUuid") UUID teacherUuid, @Param("periodUuid") UUID periodUuid);

    @Query("SELECT cs FROM ClassSessionEntity cs WHERE cs.period.uuid = :periodUuid")
    List<ClassSessionEntity> findByPeriod(@Param("periodUuid") UUID periodUuid);




    @Query("SELECT cs FROM ClassSessionEntity cs WHERE cs.studentGroup.uuid = :studentGroupUuid")
    List<ClassSessionEntity> findByStudentGroupUuid(@Param("studentGroupUuid") UUID studentGroupUuid);


    // Buscar sesiones por docente
    List<ClassSessionEntity> findByTeacherUuid(UUID teacherUuid);

    // Buscar sesiones por día de la semana
    List<ClassSessionEntity> findByDayOfWeek(DayOfWeek dayOfWeek);

    // Buscar sesiones por docente y día
    List<ClassSessionEntity> findByTeacherUuidAndDayOfWeek(UUID teacherUuid, DayOfWeek dayOfWeek);

    // Buscar sesiones por grupo y día
    List<ClassSessionEntity> findByStudentGroupUuidAndDayOfWeek(UUID studentGroupUuid, DayOfWeek dayOfWeek);

    // Buscar sesiones por espacio de aprendizaje
    List<ClassSessionEntity> findByLearningSpaceUuid(UUID learningSpaceUuid);

    // Buscar sesiones por espacio y día
    List<ClassSessionEntity> findByLearningSpaceUuidAndDayOfWeek(UUID learningSpaceUuid, DayOfWeek dayOfWeek);

    // Buscar sesiones por curso
    List<ClassSessionEntity> findByCourseUuid(UUID courseUuid);

    // Verificar conflictos de horario para docente
    @Query("SELECT cs FROM ClassSessionEntity cs " +
            "JOIN cs.teachingHours th " +
            "WHERE cs.teacher.uuid = :teacherUuid " +
            "AND cs.dayOfWeek = :dayOfWeek " +
            "AND cs.period.uuid = :periodUuid " +
            "AND th.uuid IN :teachingHourUuids")
    List<ClassSessionEntity> findTeacherConflicts(
            @Param("teacherUuid") UUID teacherUuid,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("periodUuid") UUID periodUuid,
            @Param("teachingHourUuids") List<UUID> teachingHourUuids);

    // Verificar conflictos de aula
    @Query("SELECT cs FROM ClassSessionEntity cs " +
            "JOIN cs.teachingHours th " +
            "WHERE cs.learningSpace.uuid = :learningSpaceUuid " +
            "AND cs.dayOfWeek = :dayOfWeek " +
            "AND cs.period.uuid = :periodUuid " +
            "AND th.uuid IN :teachingHourUuids")
    List<ClassSessionEntity> findLearningSpaceConflicts(
            @Param("learningSpaceUuid") UUID learningSpaceUuid,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("periodUuid") UUID periodUuid,
            @Param("teachingHourUuids") List<UUID> teachingHourUuids);

    // Verificar conflictos de grupo
    @Query("SELECT cs FROM ClassSessionEntity cs " +
            "JOIN cs.teachingHours th " +
            "WHERE cs.studentGroup.uuid = :studentGroupUuid " +
            "AND cs.dayOfWeek = :dayOfWeek " +
            "AND cs.period.uuid = :periodUuid " +
            "AND th.uuid IN :teachingHourUuids")
    List<ClassSessionEntity> findStudentGroupConflicts(
            @Param("studentGroupUuid") UUID studentGroupUuid,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("periodUuid") UUID periodUuid,
            @Param("teachingHourUuids") List<UUID> teachingHourUuids);

    // Buscar por ciclo (útil para reportes)
    @Query("SELECT cs FROM ClassSessionEntity cs " +
            "WHERE cs.studentGroup.cycle.uuid = :cycleUuid")
    List<ClassSessionEntity> findByCycleUuid(@Param("cycleUuid") UUID cycleUuid);

    // Buscar por carrera
    @Query("SELECT cs FROM ClassSessionEntity cs " +
            "WHERE cs.studentGroup.cycle.career.uuid = :careerUuid")
    List<ClassSessionEntity> findByCareerUuid(@Param("careerUuid") UUID careerUuid);

    // Contar horas asignadas por curso
    @Query("SELECT COUNT(th) FROM ClassSessionEntity cs " +
            "JOIN cs.teachingHours th " +
            "WHERE cs.course.uuid = :courseUuid")
    Long countTeachingHoursByCourse(@Param("courseUuid") UUID courseUuid);
}

