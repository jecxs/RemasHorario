package com.pontificia.remashorario.modules.learningSpace;

import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningSpaceRepository extends BaseRepository<LearningSpaceEntity> {

    List<LearningSpaceEntity>  findByTypeUUID_Name (TeachingTypeEntity.ETeachingType name);

    List<LearningSpaceEntity> findByCapacityGreaterThanEqual(Integer capacidad);

    List<LearningSpaceEntity> findByTypeUUID_NameAndCapacityGreaterThanEqual(TeachingTypeEntity.ETeachingType name, Integer capacidad);

    boolean existsByName(String nombre);
}
