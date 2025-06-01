package com.pontificia.remashorario.modules.period;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.period.dto.PeriodRequestDTO;
import com.pontificia.remashorario.modules.period.dto.PeriodResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/protected/periods")
@RequiredArgsConstructor
public class PeriodController {

    private final PeriodService periodService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PeriodResponseDTO>>> getAllPeriods() {
        return ResponseEntity.ok(ApiResponse.success(periodService.getAllPeriods(), "Periodos recuperados con éxito"));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<PeriodResponseDTO>> getPeriod(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(periodService.getPeriodById(uuid), "Periodo recuperado con éxito"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PeriodResponseDTO>> createPeriod(@Valid @RequestBody PeriodRequestDTO dto) {
        PeriodResponseDTO created = periodService.createPeriod(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Periodo creado con éxito"));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<PeriodResponseDTO>> updatePeriod(@PathVariable UUID uuid, @Valid @RequestBody PeriodRequestDTO dto) {
        PeriodResponseDTO updated = periodService.updatePeriod(uuid, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Periodo actualizado con éxito"));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deletePeriod(@PathVariable UUID uuid) {
        periodService.deletePeriod(uuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Periodo eliminado con éxito"));
    }
}
