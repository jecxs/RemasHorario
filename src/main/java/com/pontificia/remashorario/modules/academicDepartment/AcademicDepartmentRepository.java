package com.pontificia.remashorario.modules.academicDepartment;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicDepartmentRepository extends BaseRepository<AcademicDepartmentEntity> {

    Optional<AcademicDepartmentEntity> findByName(String name);

    Optional<AcademicDepartmentEntity> findByCode(String code);

    boolean existsByName(String name);

    boolean existsByCode(String code);
}
