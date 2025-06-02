package com.pontificia.remashorario.modules.TimeSlot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlotEntity, UUID> {

    // Usando consulta SQL nativa para ser más específico sobre los tipos
    @Query(value = "SELECT * FROM time_slot WHERE start_time = CAST(:startTime AS TIME) AND end_time = CAST(:endTime AS TIME)",
            nativeQuery = true)
    Optional<TimeSlotEntity> findByStartTimeAndEndTime(@Param("startTime") LocalTime startTime,
                                                       @Param("endTime") LocalTime endTime);

    @Query(value = "SELECT * FROM time_slot WHERE start_time < CAST(:endTime AS TIME) AND end_time > CAST(:startTime AS TIME)",
            nativeQuery = true)
    List<TimeSlotEntity> findOverlapping(@Param("startTime") LocalTime startTime,
                                         @Param("endTime") LocalTime endTime);
}