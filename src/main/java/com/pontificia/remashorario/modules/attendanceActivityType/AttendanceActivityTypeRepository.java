package com.pontificia.remashorario.modules.attendanceActivityType;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendanceActivityTypeRepository extends BaseRepository<AttendanceActivityTypeEntity> {

    Optional<AttendanceActivityTypeEntity> findByCode(String code);

    boolean existsByCode(String code);
}
