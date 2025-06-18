package com.pontificia.remashorario.modules.teacher;

import com.pontificia.remashorario.modules.KnowledgeArea.KnowledgeAreaEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherRepository extends BaseRepository<TeacherEntity> {


    @Query("SELECT t FROM TeacherEntity t JOIN t.knowledgeAreas ka WHERE ka.uuid = :knowledgeAreaUuid")
    List<TeacherEntity> findByKnowledgeAreasContaining(@Param("knowledgeAreaUuid") UUID knowledgeAreaUuid);

    Optional<TeacherEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<TeacherEntity> findByDepartmentUuid(UUID departmentUuid);

    @Query("SELECT DISTINCT t FROM TeacherEntity t " +
            "JOIN t.knowledgeAreas ka " +
            "WHERE ka.uuid IN :knowledgeAreaUuids")
    List<TeacherEntity> findByKnowledgeAreaUuids(@Param("knowledgeAreaUuids") List<UUID> knowledgeAreaUuids);

    @Query("SELECT DISTINCT t FROM TeacherEntity t " +
            "WHERE t.department.uuid = :departmentUuid " +
            "AND EXISTS (SELECT 1 FROM t.knowledgeAreas ka WHERE ka.uuid IN :knowledgeAreaUuids)")
    List<TeacherEntity> findByDepartmentAndKnowledgeAreas(
            @Param("departmentUuid") UUID departmentUuid,
            @Param("knowledgeAreaUuids") List<UUID> knowledgeAreaUuids);

    @Query("SELECT t FROM TeacherEntity t " +
            "WHERE LOWER(t.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(t.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<TeacherEntity> searchByNameOrEmail(@Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM TeacherEntity t " +
            "LEFT JOIN FETCH t.knowledgeAreas " +
            "LEFT JOIN FETCH t.department " +
            "WHERE t.uuid = :uuid")
    Optional<TeacherEntity> findByIdWithDetails(@Param("uuid") UUID uuid);

    @Query("SELECT t FROM TeacherEntity t " +
            "LEFT JOIN FETCH t.availabilities " +
            "WHERE t.uuid = :uuid")
    Optional<TeacherEntity> findByIdWithAvailabilities(@Param("uuid") UUID uuid);

    List<TeacherEntity> findByHasUserAccount(Boolean hasUserAccount);
}
