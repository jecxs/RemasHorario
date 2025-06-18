package com.pontificia.remashorario.modules.teacherAvailability;

import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;


@Repository
public interface TeacherAvailabilityRepository extends BaseRepository<TeacherAvailabilityEntity> {

    List<TeacherAvailabilityEntity> findByTeacherUuid(UUID teacherUuid);

    List<TeacherAvailabilityEntity> findByTeacherUuidAndDayOfWeek(UUID teacherUuid, DayOfWeek dayOfWeek);

    // CORREGIDO: Query nativa para SQL Server con conversión explícita de tipos
    @Query(value = """
        SELECT * FROM dbo.teacher_availability 
        WHERE teacher_id = :teacherUuid 
        AND day_of_week = :dayOfWeek 
        AND (
            (start_time <= CAST(:startTime AS TIME) AND end_time > CAST(:startTime AS TIME)) OR
            (start_time < CAST(:endTime AS TIME) AND end_time >= CAST(:endTime AS TIME)) OR
            (start_time >= CAST(:startTime AS TIME) AND end_time <= CAST(:endTime AS TIME))
        )
        """, nativeQuery = true)
    List<TeacherAvailabilityEntity> findOverlapping(
            @Param("teacherUuid") UUID teacherUuid,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    // CORREGIDO: También con query nativa para consistencia
    @Query(value = """
        SELECT * FROM dbo.teacher_availability 
        WHERE teacher_id = :teacherUuid 
        AND day_of_week = :dayOfWeek 
        AND start_time <= CAST(:startTime AS TIME) 
        AND end_time >= CAST(:endTime AS TIME)
        """, nativeQuery = true)
    List<TeacherAvailabilityEntity> findContaining(
            @Param("teacherUuid") UUID teacherUuid,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    void deleteByTeacherUuid(UUID teacherUuid);
    List<TeacherAvailabilityEntity> findByTeacherAndDayOfWeek(TeacherEntity teacher, DayOfWeek dayOfWeek);
    List<TeacherAvailabilityEntity> findByTeacher(TeacherEntity teacher);
    List<TeacherAvailabilityEntity> findByTeacherAndIsAvailableTrue(TeacherEntity teacher);

}