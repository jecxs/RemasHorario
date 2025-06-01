package com.pontificia.remashorario.modules.period;

import com.pontificia.remashorario.modules.period.dto.PeriodRequestDTO;
import com.pontificia.remashorario.modules.period.dto.PeriodResponseDTO;
import com.pontificia.remashorario.modules.period.mapper.PeriodMapper;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PeriodService extends BaseService<PeriodEntity> {

    private final PeriodRepository periodRepository;
    private final PeriodMapper periodMapper;

    public PeriodService(PeriodRepository periodRepository, PeriodMapper periodMapper) {
        super(periodRepository);
        this.periodRepository = periodRepository;
        this.periodMapper = periodMapper;
    }

    public List<PeriodResponseDTO> getAllPeriods() {
        return periodMapper.toResponseDTOList(findAll());
    }

    public PeriodResponseDTO getPeriodById(UUID uuid) {
        return periodMapper.toResponseDTO(findPeriodOrThrow(uuid));
    }

    @Transactional
    public PeriodResponseDTO createPeriod(PeriodRequestDTO dto) {
        if (periodRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Ya existe un periodo con ese nombre");
        }
        PeriodEntity entity = periodMapper.toEntity(dto);
        return periodMapper.toResponseDTO(save(entity));
    }

    @Transactional
    public PeriodResponseDTO updatePeriod(UUID uuid, PeriodRequestDTO dto) {
        PeriodEntity period = findPeriodOrThrow(uuid);
        if (!period.getName().equals(dto.getName()) && periodRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Ya existe otro periodo con ese nombre");
        }
        periodMapper.updateEntityFromDTO(period, dto);
        return periodMapper.toResponseDTO(save(period));
    }

    @Transactional
    public void deletePeriod(UUID uuid) {
        PeriodEntity period = findPeriodOrThrow(uuid);
        deleteById(period.getUuid());
    }

    public PeriodEntity findPeriodOrThrow(UUID uuid) {
        return findById(uuid).orElseThrow(() -> new EntityNotFoundException("Periodo no encontrado con ID: " + uuid));
    }
}
