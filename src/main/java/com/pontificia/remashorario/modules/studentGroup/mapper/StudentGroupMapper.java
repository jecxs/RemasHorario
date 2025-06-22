package com.pontificia.remashorario.modules.studentGroup.mapper;

import com.pontificia.remashorario.modules.cycle.CycleEntity;
import com.pontificia.remashorario.modules.cycle.CycleService;
import com.pontificia.remashorario.modules.period.PeriodEntity;
import com.pontificia.remashorario.modules.period.PeriodService;
import com.pontificia.remashorario.modules.studentGroup.StudentGroupEntity;
import com.pontificia.remashorario.modules.studentGroup.dto.StudentGroupRequestDTO;
import com.pontificia.remashorario.modules.studentGroup.dto.StudentGroupResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StudentGroupMapper {

    private final CycleService cycleService;
    private final PeriodService periodService;
    // private final CareerMapper careerMapper; // Solo si CycleResponseDTO en StudentGroupResponseDTO anida CareerResponseDTO

    public StudentGroupMapper(CycleService cycleService, PeriodService periodService) {
        this.cycleService = cycleService;
        this.periodService = periodService;
        // this.careerMapper = careerMapper; // Si descomentas lo de arriba, descomenta esto también
    }

    /**
     * Convierte una entidad StudentGroupEntity a un StudentGroupResponseDTO.
     *
     * @param entity La entidad StudentGroupEntity a convertir.
     * @return El StudentGroupResponseDTO resultante.
     */
    public StudentGroupResponseDTO toResponseDTO(StudentGroupEntity entity) {
        if (entity == null) {
            return null;
        }

        return StudentGroupResponseDTO.builder()
                .uuid(entity.getUuid())
                .name(entity.getName())
                .cycleUuid(entity.getCycle() != null ? entity.getCycle().getUuid() : null)
                .cycleNumber(entity.getCycle() != null ? entity.getCycle().getNumber() : null)
                .periodUuid(entity.getPeriod() != null ? entity.getPeriod().getUuid() : null)
                .periodName(entity.getPeriod() != null ? entity.getPeriod().getName() : null)

                // ✅ AGREGAR: Información de la carrera
                .careerUuid(entity.getCycle() != null && entity.getCycle().getCareer() != null ?
                        entity.getCycle().getCareer().getUuid() : null)
                .careerName(entity.getCycle() != null && entity.getCycle().getCareer() != null ?
                        entity.getCycle().getCareer().getName() : null)
                .modalityUuid(entity.getCycle() != null &&
                        entity.getCycle().getCareer() != null &&
                        entity.getCycle().getCareer().getModality() != null ?
                        entity.getCycle().getCareer().getModality().getUuid() : null)
                .modalityName(entity.getCycle() != null &&
                        entity.getCycle().getCareer() != null &&
                        entity.getCycle().getCareer().getModality() != null ?
                        entity.getCycle().getCareer().getModality().getName() : null)
                .build();
    }

    /**
     * Convierte un StudentGroupRequestDTO a una nueva entidad StudentGroupEntity.
     * Busca la entidad CycleEntity por el UUID proporcionado en el DTO de solicitud
     * usando el método `findCycleOrThrow` de `CycleService`.
     *
     * @param requestDTO El DTO de solicitud con los datos del grupo.
     * @return Una nueva instancia de StudentGroupEntity.
     */
    public StudentGroupEntity toEntity(StudentGroupRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }

        StudentGroupEntity entity = new StudentGroupEntity();
        entity.setName(requestDTO.getName());

        // Busca el CycleEntity por su UUID usando el servicio existente
        CycleEntity cycle = cycleService.findCycleOrThrow(requestDTO.getCycleUuid());
        entity.setCycle(cycle);

        PeriodEntity period = periodService.findPeriodOrThrow(requestDTO.getPeriodUuid());
        entity.setPeriod(period);

        return entity;
    }

    /**
     * Actualiza una entidad StudentGroupEntity existente con los datos de un StudentGroupRequestDTO.
     * Busca la entidad CycleEntity por el UUID proporcionado en el DTO de solicitud si se actualiza
     * usando el método `findCycleOrThrow` de `CycleService`.
     *
     * @param requestDTO El DTO de solicitud con los datos actualizados.
     * @param entity     La entidad StudentGroupEntity a actualizar.
     */
    public void updateEntityFromDTO(StudentGroupRequestDTO requestDTO, StudentGroupEntity entity) {
        if (requestDTO == null || entity == null) {
            return;
        }

        entity.setName(requestDTO.getName());

        // Si el cycleUuid cambia, busca y actualiza el CycleEntity usando el servicio existente
        if (requestDTO.getCycleUuid() != null &&
                (entity.getCycle() == null || !requestDTO.getCycleUuid().equals(entity.getCycle().getUuid()))) {
            CycleEntity newCycle = cycleService.findCycleOrThrow(requestDTO.getCycleUuid());
            entity.setCycle(newCycle);
        }

        if (requestDTO.getPeriodUuid() != null &&
                (entity.getPeriod() == null || !requestDTO.getPeriodUuid().equals(entity.getPeriod().getUuid()))) {
            PeriodEntity newPeriod = periodService.findPeriodOrThrow(requestDTO.getPeriodUuid());
            entity.setPeriod(newPeriod);
        }
    }

    /**
     * Convierte una lista de StudentGroupEntity a una lista de StudentGroupResponseDTO.
     *
     * @param entities La lista de entidades a convertir.
     * @return Una lista de StudentGroupResponseDTO.
     */
    public List<StudentGroupResponseDTO> toResponseDTOList(List<StudentGroupEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}
