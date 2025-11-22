package com.pontificia.remashorario.modules.defaultRate;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DefaultRateRepository extends BaseRepository<DefaultRateEntity> {

    @Query("SELECT dr FROM DefaultRateEntity dr " +
            "WHERE dr.activityType.uuid = :activityTypeUuid " +
            "AND (:date BETWEEN dr.effectiveFrom AND dr.effectiveTo " +
            "OR (dr.effectiveFrom <= :date AND dr.effectiveTo IS NULL))")
    Optional<DefaultRateEntity> findActiveRateByActivityType(
            @Param("activityTypeUuid") UUID activityTypeUuid,
            @Param("date") LocalDate date
    );

    List<DefaultRateEntity> findByActivityTypeUuid(UUID activityTypeUuid);

    @Query("SELECT dr FROM DefaultRateEntity dr " +
            "WHERE :date BETWEEN dr.effectiveFrom AND dr.effectiveTo " +
            "OR (dr.effectiveFrom <= :date AND dr.effectiveTo IS NULL)")
    List<DefaultRateEntity> findActiveRates(@Param("date") LocalDate date);

    @Query("SELECT dr FROM DefaultRateEntity dr " +
            "LEFT JOIN FETCH dr.activityType " +
            "WHERE dr.uuid = :uuid")
    Optional<DefaultRateEntity> findByIdWithDetails(@Param("uuid") UUID uuid);
}
