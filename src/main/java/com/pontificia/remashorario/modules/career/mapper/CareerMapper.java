package com.pontificia.remashorario.modules.career.mapper;

import com.pontificia.remashorario.modules.career.CareerEntity;
import com.pontificia.remashorario.modules.career.dto.CareerResponseDTO;
import com.pontificia.remashorario.modules.cycle.CycleEntity;
import com.pontificia.remashorario.modules.cycle.dto.CycleResponseDTO;
import com.pontificia.remashorario.modules.educationalModality.EducationalModalityEntity;
import com.pontificia.remashorario.modules.educationalModality.dto.EducationalModalityResponseDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CareerMapper {

    /**
     * Convierte una entidad CareerEntity a su DTO correspondiente CareerResponseDTO.
     *
     * @param entity Entidad CareerEntity a convertir.
     * @return DTO CareerResponseDTO que representa la entidad Career.
     */
    public static CareerResponseDTO toDto(CareerEntity entity) {
        if (entity == null) return null;

        return CareerResponseDTO.builder()
                .uuid(entity.getUuid())
                .name(entity.getName())
                .modality(toModalityDto(entity.getModality()))
                .cycles(toCycleDtoList(entity.getCycles()))
                .build();
    }

    /**
     * Convierte una entidad EducationalModalityEntity a su DTO.
     *
     * @param modality Entidad a convertir.
     * @return DTO correspondiente.
     */
    private static EducationalModalityResponseDTO toModalityDto(EducationalModalityEntity modality) {
        if (modality == null) return null;

        return EducationalModalityResponseDTO.builder()
                .uuid(modality.getUuid())
                .name(modality.getName())
                .durationYears(modality.getDurationYears())
                .build();
    }

    /**
     * Convierte una lista de CycleEntity a una lista de CycleResponseDTO.
     *
     * @param cycles Lista de entidades CycleEntity.
     * @return Lista de DTOs CycleResponseDTO.
     */
    private static List<CycleResponseDTO> toCycleDtoList(List<CycleEntity> cycles) {
        if (cycles == null) return new ArrayList<>();

        return cycles.stream()
                .map(CareerMapper::toCycleDtoBasic)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad CycleEntity a CycleResponseDTO básico (sin incluir la carrera para evitar recursión).
     *
     * @param cycle Entidad CycleEntity a convertir.
     * @return DTO CycleResponseDTO básico.
     */
    private static CycleResponseDTO toCycleDtoBasic(CycleEntity cycle) {
        if (cycle == null) return null;

        return CycleResponseDTO.builder()
                .uuid(cycle.getUuid())
                .number(cycle.getNumber())
                .career(null) // No incluir la carrera para evitar recursión circular
                .build();
    }
}
