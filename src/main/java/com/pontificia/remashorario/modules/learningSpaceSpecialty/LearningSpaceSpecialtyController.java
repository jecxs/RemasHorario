package com.pontificia.remashorario.modules.learningSpaceSpecialty;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.learningSpaceSpecialty.dto.LearningSpaceSpecialtyRequestDTO;
import com.pontificia.remashorario.modules.learningSpaceSpecialty.dto.LearningSpaceSpecialtyResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/protected/learning-space-specialties")
@RequiredArgsConstructor
public class LearningSpaceSpecialtyController {

    private final LearningSpaceSpecialtyService specialtyService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LearningSpaceSpecialtyResponseDTO>>> getAll() {
        List<LearningSpaceSpecialtyResponseDTO> list = specialtyService.getAllSpecialties();
        return ResponseEntity.ok(ApiResponse.success(list, "Especialidades recuperadas con éxito"));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<LearningSpaceSpecialtyResponseDTO>> getById(@PathVariable UUID uuid) {
        LearningSpaceSpecialtyResponseDTO dto = specialtyService.getSpecialtyById(uuid);
        return ResponseEntity.ok(ApiResponse.success(dto, "Especialidad recuperada con éxito"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LearningSpaceSpecialtyResponseDTO>> create(@Valid @RequestBody LearningSpaceSpecialtyRequestDTO dto) {
        LearningSpaceSpecialtyResponseDTO created = specialtyService.createSpecialty(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Especialidad creada con éxito"));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<LearningSpaceSpecialtyResponseDTO>> update(@PathVariable UUID uuid,
            @Valid @RequestBody LearningSpaceSpecialtyRequestDTO dto) {
        LearningSpaceSpecialtyResponseDTO updated = specialtyService.updateSpecialty(uuid, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Especialidad actualizada con éxito"));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID uuid) {
        specialtyService.deleteSpecialty(uuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Especialidad eliminada con éxito"));
    }
}
