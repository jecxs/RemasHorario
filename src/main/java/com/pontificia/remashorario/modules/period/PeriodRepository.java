package com.pontificia.remashorario.modules.period;

import com.pontificia.remashorario.utils.abstractBase.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodRepository extends BaseRepository<PeriodEntity> {
    boolean existsByName(String name);
}
