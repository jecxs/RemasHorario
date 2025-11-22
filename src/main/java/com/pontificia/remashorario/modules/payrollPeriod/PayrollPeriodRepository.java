package com.pontificia.remashorario.modules.payrollPeriod;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollPeriodRepository extends BaseRepository<PayrollPeriodEntity> {

    List<PayrollPeriodEntity> findByStatus(PayrollPeriodEntity.PayrollStatus status);

    @Query("SELECT pp FROM PayrollPeriodEntity pp " +
            "WHERE :date BETWEEN pp.startDate AND pp.endDate")
    Optional<PayrollPeriodEntity> findByDate(@Param("date") LocalDate date);

    @Query("SELECT pp FROM PayrollPeriodEntity pp " +
            "WHERE pp.startDate <= :endDate AND pp.endDate >= :startDate")
    List<PayrollPeriodEntity> findOverlappingPeriods(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT pp FROM PayrollPeriodEntity pp " +
            "WHERE pp.endDate < :date " +
            "ORDER BY pp.endDate DESC")
    List<PayrollPeriodEntity> findPastPeriods(@Param("date") LocalDate date);

    @Query("SELECT pp FROM PayrollPeriodEntity pp " +
            "WHERE pp.startDate > :date " +
            "ORDER BY pp.startDate ASC")
    List<PayrollPeriodEntity> findFuturePeriods(@Param("date") LocalDate date);

    @Query("SELECT pp FROM PayrollPeriodEntity pp " +
            "WHERE pp.status IN ('DRAFT', 'CALCULATED') " +
            "ORDER BY pp.startDate ASC")
    List<PayrollPeriodEntity> findPendingPeriods();

    @Query("SELECT pp FROM PayrollPeriodEntity pp " +
            "ORDER BY pp.startDate DESC")
    List<PayrollPeriodEntity> findAllOrderByStartDateDesc();

    @Query("SELECT CASE WHEN COUNT(pp) > 0 THEN true ELSE false END " +
            "FROM PayrollPeriodEntity pp " +
            "WHERE pp.startDate <= :endDate AND pp.endDate >= :startDate")
    boolean existsOverlappingPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
