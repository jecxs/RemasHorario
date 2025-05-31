package com.pontificia.remashorario.modules.teachingHour;

import com.pontificia.remashorario.modules.TimeSlot.TimeSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeachingHourRepository extends JpaRepository<TeachingHourEntity, UUID> {

}
