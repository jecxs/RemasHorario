package com.pontificia.remashorario.modules.course;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends BaseRepository<CourseEntity> {

    List<CourseEntity> findByCycleUuid(UUID cycleId);

    Optional<CourseEntity> findByNameAndCycleUuid(String name, UUID cycleId);

    boolean existsByNameAndCycleUuid(String name, UUID cycleId);

    List<CourseEntity> findByNameContainingIgnoreCase(String name);

    @Query("SELECT c FROM CourseEntity c WHERE c.cycle.career.uuid = :careerUuid")
    List<CourseEntity> findByCareerUuid(@Param("careerUuid") UUID careerUuid);

    @Query("SELECT c FROM CourseEntity c WHERE c.cycle.career.modality.uuid = :modalityUuid")
    List<CourseEntity> findByModalityUuid(@Param("modalityUuid") UUID modalityUuid);

    @Query("SELECT c FROM CourseEntity c WHERE c.teachingKnowledgeArea.uuid = :knowledgeAreaUuid")
    List<CourseEntity> findByKnowledgeAreaUuid(@Param("knowledgeAreaUuid") UUID knowledgeAreaUuid);

    @Query("SELECT c FROM CourseEntity c WHERE c.cycle.uuid = :cycleUuid AND c.cycle.career.uuid = :careerUuid")
    List<CourseEntity> findByCycleUuidAndCareerUuid(@Param("cycleUuid") UUID cycleUuid, @Param("careerUuid") UUID careerUuid);
}
