package com.pontificia.remashorario.modules.cycle;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CycleService {

    private final CycleRepository cycleRepository;

    /**
     * Crea un nuevo ciclo académico.
     *
     * @param request Entidad CycleEntity con la información del ciclo a registrar.
     * @return El ciclo creado y persistido en la base de datos.
     * @throws IllegalArgumentException si los datos requeridos no son válidos.
     */
    @Transactional
    public CycleEntity createCycle(CycleEntity request) {
        if (request.getNumber() == null || request.getCareer() == null) {
            throw new IllegalArgumentException("El número del ciclo y el curso son obligatorios.");
        }

        return cycleRepository.save(request);
    }

    /**
     * Busca un ciclo académico por su ID. Si no se encuentra, lanza una excepción.
     *
     * @param uuid UUID del ciclo a buscar.
     * @return El ciclo académico correspondiente al UUID proporcionado.
     * @throws EntityNotFoundException si no se encuentra un ciclo con el UUID dado.
     */
    public CycleEntity findCycleOrThrow(UUID uuid) {
        return cycleRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Cycle not found with ID: " + uuid));
    }
}