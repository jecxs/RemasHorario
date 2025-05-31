package com.pontificia.remashorario.modules.studentGroup;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.studentGroup.dto.StudentGroupRequestDTO;
import com.pontificia.remashorario.modules.studentGroup.dto.StudentGroupResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/protected/student-groups") // Ruta para tus endpoints
@RequiredArgsConstructor
public class StudentGroupController {

    private final StudentGroupService studentGroupService;

    /**
     * Obtiene todos los grupos de estudiantes.
     *
     * @return Respuesta con lista de DTOs de grupos de estudiantes.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentGroupResponseDTO>>> getAllStudentGroups() {
        List<StudentGroupResponseDTO> studentGroups = studentGroupService.getAllStudentGroups();
        return ResponseEntity.ok(
                ApiResponse.success(studentGroups, "Grupos de estudiantes recuperados con éxito")
        );
    }

    /**
     * Obtiene un grupo de estudiantes por su UUID.
     *
     * @param uuid UUID del grupo de estudiantes.
     * @return Respuesta con el DTO del grupo de estudiantes.
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<StudentGroupResponseDTO>> getStudentGroupByUuid(@PathVariable UUID uuid) {
        StudentGroupResponseDTO studentGroup = studentGroupService.getStudentGroupByUuid(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(studentGroup, "Grupo de estudiantes recuperado con éxito")
        );
    }

    /**
     * Crea un nuevo grupo de estudiantes.
     *
     * @param requestDTO DTO con los datos para crear el grupo.
     * @return Respuesta con el DTO del grupo creado.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StudentGroupResponseDTO>> createStudentGroup(
            @Valid @RequestBody StudentGroupRequestDTO requestDTO) {
        StudentGroupResponseDTO createdStudentGroup = studentGroupService.createStudentGroup(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdStudentGroup, "Grupo de estudiantes creado con éxito"));
    }

    /**
     * Actualiza un grupo de estudiantes existente.
     *
     * @param uuid       UUID del grupo de estudiantes a actualizar.
     * @param requestDTO DTO con los nuevos datos para actualizar el grupo.
     * @return Respuesta con el DTO del grupo actualizado.
     */
    @PatchMapping("/{uuid}")
    public ResponseEntity<ApiResponse<StudentGroupResponseDTO>> updateStudentGroup(
            @PathVariable UUID uuid,
            @Valid @RequestBody StudentGroupRequestDTO requestDTO) {
        StudentGroupResponseDTO updatedStudentGroup = studentGroupService.updateStudentGroup(uuid, requestDTO);
        return ResponseEntity.ok(
                ApiResponse.success(updatedStudentGroup, "Grupo de estudiantes actualizado con éxito")
        );
    }

    /**
     * Elimina un grupo de estudiantes por su UUID.
     *
     * @param uuid UUID del grupo de estudiantes a eliminar.
     * @return Respuesta con un mensaje de éxito.
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteStudentGroup(@PathVariable UUID uuid) {
        studentGroupService.deleteStudentGroup(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Grupo de estudiantes eliminado con éxito")
        );
    }
}
