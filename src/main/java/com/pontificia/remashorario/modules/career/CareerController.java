package com.pontificia.remashorario.modules.career;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.career.dto.CareerRequestDTO;
import com.pontificia.remashorario.modules.career.dto.CareerResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/protected/career")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;

    /**
     * Obtiene todas las carreras registradas en el sistema.
     *
     * @return Lista de carreras junto con un mensaje de éxito.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CareerResponseDTO>>> getAllCareers() {
        List<CareerResponseDTO> response = careerService.getAllCareers();
        return ResponseEntity.ok(
                ApiResponse.success(response, "Carreras educativas recuperadas con éxito")
        );
    }

    /**
     * Crea una nueva carrera y genera automáticamente sus ciclos según los años de la modalidad.
     *
     * @param request Datos para la creación de la carrera (nombre y UUID de modalidad).
     * @return Carrera creada junto con un mensaje de éxito.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CareerResponseDTO>> createCareer(
            @Valid @RequestBody CareerRequestDTO request
    ) {
        CareerResponseDTO createdCareer = careerService.createCareer(request.name(), request.modalityId());
        return ResponseEntity.ok(
                ApiResponse.success(createdCareer, "Carrera creada exitosamente con ciclos generados")
        );
    }

    /**
     * Actualiza los datos de una carrera existente. Si se cambia la modalidad,
     * se actualiza la relación. También pueden eliminarse los ciclos antiguos
     * si la modalidad cambia (según reglas de negocio futuras).
     *
     * @param uuid    UUID de la carrera a actualizar.
     * @param request Datos de la carrera a modificar (nombre y/o modalidad).
     * @return Carrera actualizada junto con un mensaje de éxito.
     */
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<CareerResponseDTO>> updateCareer(
            @PathVariable UUID uuid,
            @Valid @RequestBody CareerRequestDTO request
    ) {
        CareerResponseDTO updated = careerService.updateCareer(uuid, request);
        return ResponseEntity.ok(
                ApiResponse.success(updated, "Carrera actualizada correctamente")
        );
    }

    /**
     * Elimina una carrera y sus ciclos asociados.
     *
     * @param uuid UUID de la carrera a eliminar.
     * @return Mensaje de éxito si la operación fue completada.
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<String>> deleteCareer(@PathVariable UUID uuid) {
        careerService.deleteCareer(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Carrera eliminada correctamente")
        );
    }
}
