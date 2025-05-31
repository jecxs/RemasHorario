package com.pontificia.remashorario.modules.teachingType;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeachingTypeRepository extends BaseRepository<TeachingTypeEntity> {
    Optional<TeachingTypeEntity> findByName(String name);
    boolean existsByName(String name);

}
