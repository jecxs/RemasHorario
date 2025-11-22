package com.pontificia.remashorario.modules.payrollLine;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollLineRepository extends BaseRepository<PayrollLineEntity> {

    List<PayrollLineEntity> findByPayrollPeriodUuid(UUID payrollPeriodUuid);

    List<PayrollLineEntity> findByTeacherUuid(UUID teacherUuid);

    @Query("SELECT pl FROM PayrollLineEntity pl " +
            "WHERE pl.payrollPeriod.uuid = :payrollPeriodUuid " +
            "AND pl.teacher.uuid = :teacherUuid")
    Optional<PayrollLineEntity> findByPayrollPeriodAndTeacher(
            @Param("payrollPeriodUuid") UUID payrollPeriodUuid,
            @Param("teacherUuid") UUID teacherUuid
    );

    @Query("SELECT pl FROM PayrollLineEntity pl " +
            "LEFT JOIN FETCH pl.payrollPeriod " +
            "LEFT JOIN FETCH pl.teacher " +
            "WHERE pl.uuid = :uuid")
    Optional<PayrollLineEntity> findByIdWithDetails(@Param("uuid") UUID uuid);

    @Query("SELECT pl FROM PayrollLineEntity pl " +
            "LEFT JOIN FETCH pl.payrollPeriod " +
            "LEFT JOIN FETCH pl.teacher " +
            "WHERE pl.payrollPeriod.uuid = :payrollPeriodUuid")
    List<PayrollLineEntity> findByPayrollPeriodWithDetails(@Param("payrollPeriodUuid") UUID payrollPeriodUuid);

    @Query("SELECT pl FROM PayrollLineEntity pl " +
            "WHERE pl.teacher.uuid = :teacherUuid " +
            "ORDER BY pl.payrollPeriod.startDate DESC")
    List<PayrollLineEntity> findByTeacherOrderByPeriodDesc(@Param("teacherUuid") UUID teacherUuid);

    @Query("SELECT COALESCE(SUM(pl.netAmount), 0) " +
            "FROM PayrollLineEntity pl " +
            "WHERE pl.payrollPeriod.uuid = :payrollPeriodUuid")
    java.math.BigDecimal calculateTotalNetAmountByPeriod(@Param("payrollPeriodUuid") UUID payrollPeriodUuid);

    @Query("SELECT COALESCE(SUM(pl.grossAmount), 0) " +
            "FROM PayrollLineEntity pl " +
            "WHERE pl.payrollPeriod.uuid = :payrollPeriodUuid")
    java.math.BigDecimal calculateTotalGrossAmountByPeriod(@Param("payrollPeriodUuid") UUID payrollPeriodUuid);

    @Query("SELECT COALESCE(SUM(pl.totalPenalties), 0) " +
            "FROM PayrollLineEntity pl " +
            "WHERE pl.payrollPeriod.uuid = :payrollPeriodUuid")
    java.math.BigDecimal calculateTotalPenaltiesByPeriod(@Param("payrollPeriodUuid") UUID payrollPeriodUuid);

    @Query("SELECT COUNT(pl) " +
            "FROM PayrollLineEntity pl " +
            "WHERE pl.payrollPeriod.uuid = :payrollPeriodUuid")
    Long countByPayrollPeriod(@Param("payrollPeriodUuid") UUID payrollPeriodUuid);
}
