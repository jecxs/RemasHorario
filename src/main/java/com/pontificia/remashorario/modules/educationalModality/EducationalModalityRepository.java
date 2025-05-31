package com.pontificia.remashorario.modules.educationalModality;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationalModalityRepository extends BaseRepository<EducationalModalityEntity> {
    boolean existsByName(String name);
}

