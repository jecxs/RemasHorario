package com.pontificia.remashorario.modules.classSession;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClassSessionRepository extends BaseRepository<ClassSessionEntity> {

    // Buscar sesiones por grupo de estudiantes
    List<ClassSessionEntity> findByStudentGroupUuid(UUID studentGroupUuid);

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

