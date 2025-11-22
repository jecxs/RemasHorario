package com.pontificia.remashorario.modules.modalityRate;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModalityRateRepository extends BaseRepository<ModalityRateEntity> {

    List<ModalityRateEntity> findByModalityUuid(UUID modalityUuid);

    @Query("SELECT mr FROM ModalityRateEntity mr " +
            "WHERE mr.modality.uuid = :modalityUuid " +
            "AND mr.activityType.uuid = :activityTypeUuid " +
            "AND (:date BETWEEN mr.effectiveFrom AND mr.effectiveTo " +
            "OR (mr.effectiveFrom <= :date AND mr.effectiveTo IS NULL))")
    Optional<ModalityRateEntity> findActiveRateByModalityAndActivityType(
            @Param("modalityUuid") UUID modalityUuid,
            @Param("activityTypeUuid") UUID activityTypeUuid,
            @Param("date") LocalDate date
    );

    @Query("SELECT mr FROM ModalityRateEntity mr " +
            "WHERE mr.modality.uuid = :modalityUuid " +
            "AND (:date BETWEEN mr.effectiveFrom AND mr.effectiveTo " +
            "OR (mr.effectiveFrom <= :date AND mr.effectiveTo IS NULL))")
    List<ModalityRateEntity> findActiveRatesByModality(
            @Param("modalityUuid") UUID modalityUuid,
            @Param("date") LocalDate date
    );

    List<ModalityRateEntity> findByActivityTypeUuid(UUID activityTypeUuid);

    @Query("SELECT mr FROM ModalityRateEntity mr " +
            "WHERE mr.modality.uuid = :modalityUuid " +
            "AND mr.activityType.uuid = :activityTypeUuid")
    List<ModalityRateEntity> findByModalityAndActivityType(
            @Param("modalityUuid") UUID modalityUuid,
            @Param("activityTypeUuid") UUID activityTypeUuid
    );

    @Query("SELECT mr FROM ModalityRateEntity mr " +
            "LEFT JOIN FETCH mr.modality " +
            "LEFT JOIN FETCH mr.activityType " +
            "WHERE mr.uuid = :uuid")
    Optional<ModalityRateEntity> findByIdWithDetails(@Param("uuid") UUID uuid);
}
