package com.pontificia.remashorario.modules.teachingType;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.teachingType.dto.TeachingTypeResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/protected/teaching-types")
@RequiredArgsConstructor
public class TeachingTypeController {

    private final TeachingTypeService teachingTypeService;

    /**
     * Obtiene todos los tipos de enseñanza disponibles.
     *
     * @return lista de tipos de enseñanza junto con un mensaje de éxito.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TeachingTypeResponseDTO>>> getAllTeachingTypes() {
        List<TeachingTypeResponseDTO> types = teachingTypeService.getAllTeachingTypes();
        return ResponseEntity.ok(
                ApiResponse.success(types, "Tipos de enseñanza recuperados con éxito")
        );
    }

    /**
     * Obtiene un tipo de enseñanza por su UUID.
     *
     * @param uuid identificador único del tipo de enseñanza
     * @return tipo de enseñanza encontrado junto con un mensaje de éxito.
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<TeachingTypeResponseDTO>> getTeachingTypeById(@PathVariable UUID uuid) {
        TeachingTypeResponseDTO dto = teachingTypeService.getTeachingTypeById(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(dto, "Tipo de enseñanza recuperado con éxito")
        );
    }
}
