package com.pontificia.remashorario.modules.academicDepartment;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.academicDepartment.dto.AcademicDepartmentRequestDTO;
import com.pontificia.remashorario.modules.academicDepartment.dto.AcademicDepartmentResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/protected/academic-departments")
@RequiredArgsConstructor
public class AcademicDepartmentController {

    private final AcademicDepartmentService departmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AcademicDepartmentResponseDTO>>> getAllDepartments() {
        List<AcademicDepartmentResponseDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(
                ApiResponse.success(departments, "Departamentos académicos recuperados con éxito")
        );
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<AcademicDepartmentResponseDTO>> getDepartmentById(@PathVariable UUID uuid) {
        AcademicDepartmentResponseDTO department = departmentService.getDepartmentById(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(department, "Departamento académico recuperado con éxito")
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AcademicDepartmentResponseDTO>> createDepartment(
            @Valid @RequestBody AcademicDepartmentRequestDTO dto) {
        AcademicDepartmentResponseDTO newDepartment = departmentService.createDepartment(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(newDepartment, "Departamento académico creado con éxito"));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<AcademicDepartmentResponseDTO>> updateDepartment(
            @PathVariable UUID uuid,
            @Valid @RequestBody AcademicDepartmentRequestDTO dto) {
        AcademicDepartmentResponseDTO updatedDepartment = departmentService.updateDepartment(uuid, dto);
        return ResponseEntity.ok(
                ApiResponse.success(updatedDepartment, "Departamento académico actualizado con éxito")
        );
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable UUID uuid) {
        departmentService.deleteDepartment(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Departamento académico eliminado con éxito")
        );
    }
}
