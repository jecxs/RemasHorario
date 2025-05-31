package com.pontificia.remashorario.modules.career;

import com.pontificia.remashorario.modules.educationalModality.EducationalModalityEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CareerRepository extends BaseRepository<CareerEntity> {

    List<CareerEntity> findByModality(EducationalModalityEntity modality);

    Optional<CareerEntity> findByNameAndModality(String name, EducationalModalityEntity modality);

    boolean existsByNameAndModality(String name, EducationalModalityEntity modality);

}
