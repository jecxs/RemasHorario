package com.pontificia.remashorario.modules.course;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.course.dto.CourseFilterDTO;
import com.pontificia.remashorario.modules.course.dto.CourseRequestDTO;
import com.pontificia.remashorario.modules.course.dto.CourseResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/protected/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> getAllCourses() {
        List<CourseResponseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(
                ApiResponse.success(courses, "Cursos recuperados con éxito")
        );
    }

    @GetMapping("/cycle/{cycleUuid}/career/{careerUuid}")
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> getCoursesByCycleAndCareer(
            @PathVariable UUID cycleUuid,
            @PathVariable UUID careerUuid) {

        List<CourseResponseDTO> courses = courseService.getCoursesByCycleAndCareer(cycleUuid, careerUuid);
        return ResponseEntity.ok(
                ApiResponse.success(courses, "Cursos recuperados con éxito")
        );
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> getCourseById(@PathVariable UUID uuid) {
        CourseResponseDTO course = courseService.getCourseById(uuid);
        return ResponseEntity.ok(
                ApiResponse.success(course, "Curso recuperado con éxito")
        );
    }

    @GetMapping("/knowledge-area/{knowledgeAreaUuid}")
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> getCoursesByKnowledgeArea(
            @PathVariable UUID knowledgeAreaUuid) {
        List<CourseResponseDTO> courses = courseService.getCoursesByKnowledgeArea(knowledgeAreaUuid);
        return ResponseEntity.ok(
                ApiResponse.success(courses, "Cursos del área recuperados con éxito")
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponseDTO>> createCourse(
            @Valid @RequestBody CourseRequestDTO courseDTO) {
        CourseResponseDTO newCourse = courseService.createCourse(courseDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(newCourse, "Curso creado con éxito"));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> updateCourse(
            @PathVariable UUID uuid,
            @Valid @RequestBody CourseRequestDTO courseDTO) {
        CourseResponseDTO updatedCourse = courseService.updateCourse(uuid, courseDTO);
        return ResponseEntity.ok(
                ApiResponse.success(updatedCourse, "Curso actualizado con éxito")
        );
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> filterCourses(
            @RequestParam(required = false) UUID modalityUuid,
            @RequestParam(required = false) UUID careerUuid,
            @RequestParam(required = false) UUID cycleUuid,
            @RequestParam(required = false) UUID knowledgeAreaUuid,
            @RequestParam(required = false) String courseName) {

        CourseFilterDTO filters = CourseFilterDTO.builder()
                .modalityUuid(modalityUuid)
                .careerUuid(careerUuid)
                .cycleUuid(cycleUuid)
                .knowledgeAreaUuid(knowledgeAreaUuid)
                .courseName(courseName)
                .build();

        List<CourseResponseDTO> courses = courseService.filterCourses(filters);
        return ResponseEntity.ok(
                ApiResponse.success(courses, "Cursos filtrados recuperados con éxito")
        );
    }
}
