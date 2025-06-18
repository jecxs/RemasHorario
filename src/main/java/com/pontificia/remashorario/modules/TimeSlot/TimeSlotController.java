package com.pontificia.remashorario.modules.TimeSlot;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.TimeSlot.dto.TimeSlotRequestDTO;
import com.pontificia.remashorario.modules.TimeSlot.dto.TimeSlotResponseDTO;
import com.pontificia.remashorario.modules.teachingHour.dto.TeachingHourResponseDTO;
import com.pontificia.remashorario.modules.teachingHour.mapper.TeachingHourMapper;
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
    private final TeachingHourMapper teachingHourMapper;

    public TimeSlotController(TimeSlotService timeSlotService,
                              TeachingHourMapper teachingHourMapper) {
        this.timeSlotService = timeSlotService;
        this.teachingHourMapper = teachingHourMapper;
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
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<TeachingHourResponseDTO>>> getAvailableTeachingHours(
            @RequestParam UUID teacherUuid,
            @RequestParam UUID spaceUuid,
            @RequestParam UUID groupUuid,
            @RequestParam String dayOfWeek) {

        List<TeachingHourResponseDTO> availableHours = teachingHourMapper.toResponseDTOList(
                timeSlotService.getAvailableHours(teacherUuid, spaceUuid, groupUuid, dayOfWeek));

        return ResponseEntity.ok(
                ApiResponse.success(availableHours, "Horas disponibles recuperadas con éxito")
        );
    }

    @GetMapping("/time-slot/{timeSlotUuid}")
    public ResponseEntity<ApiResponse<List<TeachingHourResponseDTO>>> getTeachingHoursByTimeSlot(
            @PathVariable UUID timeSlotUuid) {

        List<TeachingHourResponseDTO> hours = teachingHourMapper.toResponseDTOList(
                timeSlotService.getHoursByTimeSlot(timeSlotUuid));
        return ResponseEntity.ok(
                ApiResponse.success(hours, "Horas pedagógicas del turno recuperadas con éxito")
        );
    }

}
