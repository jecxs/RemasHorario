package com.pontificia.remashorario.modules.learningSpace;

import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningSpaceRepository extends BaseRepository<LearningSpaceEntity> {
    
    List<LearningSpaceEntity> findByTypeUUID_Name(TeachingTypeEntity.ETeachingType name);


    List<LearningSpaceEntity> findByCapacityGreaterThanEqual(Integer capacidad);

    List<LearningSpaceEntity> findByTypeUUID_NameAndCapacityGreaterThanEqual(TeachingTypeEntity.ETeachingType name, Integer capacidad);

    /**
     * Busca espacios filtrando por tipo de enseñanza y especialidad de laboratorio.
     * Si la especialidad es nula, se obtendrán las aulas sin especialidad asignada.
     */
    List<LearningSpaceEntity> findByTypeUUID_NameAndSpecialty_Uuid(TeachingTypeEntity.ETeachingType name, java.util.UUID specialtyUuid);

    List<LearningSpaceEntity> findByTypeUUID_NameAndSpecialtyIsNull(TeachingTypeEntity.ETeachingType name);

    boolean existsByName(String nombre);
}
