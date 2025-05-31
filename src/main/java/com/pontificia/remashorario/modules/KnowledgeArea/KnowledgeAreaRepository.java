package com.pontificia.remashorario.modules.KnowledgeArea;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KnowledgeAreaRepository extends BaseRepository<KnowledgeAreaEntity> {

    List<KnowledgeAreaEntity> findByDepartmentUuid(UUID departmentUuid);

    Optional<KnowledgeAreaEntity> findByNameAndDepartmentUuid(String name, UUID departmentUuid);

    boolean existsByNameAndDepartmentUuid(String name, UUID departmentUuid);

    List<KnowledgeAreaEntity> findByNameContainingIgnoreCase(String name);
}
