package com.pontificia.remashorario.modules.academicCalendarException;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicCalendarExceptionRepository extends BaseRepository<AcademicCalendarExceptionEntity> {

    Optional<AcademicCalendarExceptionEntity> findByDate(LocalDate date);

    boolean existsByDate(LocalDate date);

    @Query("SELECT ace FROM AcademicCalendarExceptionEntity ace " +
            "WHERE ace.date BETWEEN :startDate AND :endDate " +
            "ORDER BY ace.date ASC")
    List<AcademicCalendarExceptionEntity> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<AcademicCalendarExceptionEntity> findByDateAfter(LocalDate date);

    List<AcademicCalendarExceptionEntity> findByDateBefore(LocalDate date);

    Optional<AcademicCalendarExceptionEntity> findByCode(String code);
}
