package com.pontificia.remashorario.modules.TimeSlot;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.TimeSlot.dto.TimeSlotRequestDTO;
import com.pontificia.remashorario.modules.TimeSlot.dto.TimeSlotResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/protected/timeslots") // Ruta base para los turnos
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TimeSlotResponseDTO>> createTimeSlot(@Valid @RequestBody TimeSlotRequestDTO requestDTO) {
        TimeSlotResponseDTO createdTimeSlot = timeSlotService.createTimeSlot(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdTimeSlot, "Turno creado exitosamente."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeSlotResponseDTO>>> getAllTimeSlots() {
        List<TimeSlotResponseDTO> timeSlots = timeSlotService.getAllTimeSlots();
        return ResponseEntity.ok(ApiResponse.success(timeSlots, "Turnos obtenidos exitosamente."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TimeSlotResponseDTO>> getTimeSlotById(@PathVariable UUID id) {
        TimeSlotResponseDTO timeSlot = timeSlotService.getTimeSlotById(id);
        return ResponseEntity.ok(ApiResponse.success(timeSlot, "Turno obtenido exitosamente."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TimeSlotResponseDTO>> updateTimeSlot(@PathVariable UUID id, @Valid @RequestBody TimeSlotRequestDTO requestDTO) {
        TimeSlotResponseDTO updatedTimeSlot = timeSlotService.updateTimeSlot(id, requestDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedTimeSlot, "Turno actualizado exitosamente."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTimeSlot(@PathVariable UUID id) {
        timeSlotService.deleteTimeSlot(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Turno eliminado exitosamente."));
    }
}
