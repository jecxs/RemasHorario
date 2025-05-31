package com.pontificia.remashorario.modules.studentGroup;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StudentGroupRepository extends BaseRepository<StudentGroupEntity> {

    // Método para verificar si un grupo con un nombre dado ya existe para un ciclo específico
    boolean existsByNameAndCycle_Uuid(String name, UUID cycleUuid);
}
