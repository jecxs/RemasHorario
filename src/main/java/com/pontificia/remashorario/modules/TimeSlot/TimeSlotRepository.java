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

    Optional<TimeSlotEntity> findByStartTimeAndEndTime(LocalTime startTime, LocalTime endTime);

    @Query("SELECT ts FROM TimeSlotEntity ts WHERE ts.startTime < :endTime AND ts.endTime > :startTime")
    List<TimeSlotEntity> findOverlapping(@Param("startTime") LocalTime startTime,
                                         @Param("endTime") LocalTime endTime);
}
