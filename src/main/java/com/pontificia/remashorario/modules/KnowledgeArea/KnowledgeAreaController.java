package com.pontificia.remashorario.modules.KnowledgeArea;


import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.KnowledgeArea.dto.KnowledgeAreaRequestDTO;
import com.pontificia.remashorario.modules.KnowledgeArea.dto.KnowledgeAreaResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/protected/knowledge-areas")
@RequiredArgsConstructor
public class KnowledgeAreaController {

    private final KnowledgeAreaService knowledgeAreaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<KnowledgeAreaResponseDTO>>> getAllKnowledgeAreas() {
        List<KnowledgeAreaResponseDTO> areas = knowledgeAreaService.getAllKnowledgeAreas();
        return ResponseEntity.ok(
                ApiResponse.success(areas, "Áreas de conocimiento recuperadas con éxito")
        );
    }

    @GetMapping("/department/{departmentUuid}")
    public ResponseEntity<ApiResponse<List<KnowledgeAreaResponseDTO>>> getKnowledgeAreasByDepartment(
            @PathVariable UUID departmentUuid) {
        List<KnowledgeAreaResponseDTO> areas = knowledgeAreaService.getKnowledgeAreasByDepartment(departmentUuid);
        return ResponseEntity.ok(
                ApiResponse.success(areas, "Áreas de conocimiento del departamento recuperadas con éxito")
        );
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<KnowledgeAreaResponseDTO>> getKnowledgeAreaById(@PathVariable UUID uuid) {
        KnowledgeAreaResponseDTO area = knowledgeAreaService.getKnowledgeAreaById(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(area, "Área de conocimiento recuperada con éxito")
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<KnowledgeAreaResponseDTO>>> searchKnowledgeAreas(
            @RequestParam String name) {
        List<KnowledgeAreaResponseDTO> areas = knowledgeAreaService.searchByName(name);
        return ResponseEntity.ok(
                ApiResponse.success(areas, "Búsqueda de áreas de conocimiento completada")
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<KnowledgeAreaResponseDTO>> createKnowledgeArea(
            @Valid @RequestBody KnowledgeAreaRequestDTO dto) {
        KnowledgeAreaResponseDTO newArea = knowledgeAreaService.createKnowledgeArea(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(newArea, "Área de conocimiento creada con éxito"));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<KnowledgeAreaResponseDTO>> updateKnowledgeArea(
            @PathVariable UUID uuid,
            @Valid @RequestBody KnowledgeAreaRequestDTO dto) {
        KnowledgeAreaResponseDTO updatedArea = knowledgeAreaService.updateKnowledgeArea(uuid, dto);
        return ResponseEntity.ok(
                ApiResponse.success(updatedArea, "Área de conocimiento actualizada con éxito")
        );
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteKnowledgeArea(@PathVariable UUID uuid) {
        knowledgeAreaService.deleteKnowledgeArea(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Área de conocimiento eliminada con éxito")
        );
    }
}
