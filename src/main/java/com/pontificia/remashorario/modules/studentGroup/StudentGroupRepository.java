package com.pontificia.remashorario.modules.studentGroup;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentGroupRepository extends BaseRepository<StudentGroupEntity> {

    // ✅ AGREGAR: Buscar grupos por periodo
    @Query("SELECT sg FROM StudentGroupEntity sg WHERE sg.period.uuid = :periodUuid")
    List<StudentGroupEntity> findByPeriodUuid(@Param("periodUuid") UUID periodUuid);

    // ✅ AGREGAR: Buscar grupos por periodo y ciclo (útil para filtros adicionales)
    @Query("SELECT sg FROM StudentGroupEntity sg WHERE sg.period.uuid = :periodUuid AND sg.cycle.uuid = :cycleUuid")
    List<StudentGroupEntity> findByPeriodUuidAndCycleUuid(@Param("periodUuid") UUID periodUuid, @Param("cycleUuid") UUID cycleUuid);


    // Método para verificar si un grupo con un nombre dado ya existe para un ciclo y periodo específicos
    boolean existsByNameAndCycle_UuidAndPeriod_Uuid(String name, UUID cycleUuid, UUID periodUuid);


}
