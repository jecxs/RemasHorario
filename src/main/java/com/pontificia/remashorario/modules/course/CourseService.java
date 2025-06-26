package com.pontificia.remashorario.modules.course;

import com.pontificia.remashorario.modules.learningSpaceSpecialty.LearningSpaceSpecialtyEntity;
import com.pontificia.remashorario.modules.learningSpaceSpecialty.LearningSpaceSpecialtyService;
import com.pontificia.remashorario.modules.KnowledgeArea.KnowledgeAreaEntity;
import com.pontificia.remashorario.modules.KnowledgeArea.KnowledgeAreaService;
import com.pontificia.remashorario.modules.course.dto.CourseFilterDTO;
import com.pontificia.remashorario.modules.course.dto.CourseRequestDTO;
import com.pontificia.remashorario.modules.course.dto.CourseResponseDTO;
import com.pontificia.remashorario.modules.course.mapper.CourseMapper;
import com.pontificia.remashorario.modules.cycle.CycleEntity;
import com.pontificia.remashorario.modules.cycle.CycleService;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeService;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CourseService extends BaseService<CourseEntity> {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final TeachingTypeService teachingTypeService;
    private final CycleService cycleService;
    private final KnowledgeAreaService knowledgeAreaService;
    private final LearningSpaceSpecialtyService specialtyService;

    @Autowired
    public CourseService(CourseRepository courseRepository,
                         CourseMapper courseMapper,
                         TeachingTypeService teachingTypeService,
                         CycleService cycleService,
                         KnowledgeAreaService knowledgeAreaService,
                         LearningSpaceSpecialtyService specialtyService) {
        super(courseRepository);
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
        this.teachingTypeService = teachingTypeService;
        this.cycleService = cycleService;
        this.knowledgeAreaService = knowledgeAreaService;
        this.specialtyService = specialtyService;
    }

    public List<CourseResponseDTO> getAllCourses() {
        List<CourseEntity> courses = findAll();
        return courseMapper.toResponseDTOList(courses);
    }

    public CourseResponseDTO getCourseById(UUID uuid) {
        CourseEntity course = findCourseOrThrow(uuid);
        return courseMapper.toResponseDTO(course);
    }

    public CourseEntity findCourseOrThrow(UUID uuid) {
        return findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with ID: " + uuid));
    }

    public java.util.List<CourseEntity> getCoursesByCycle(java.util.UUID cycleUuid) {
        return courseRepository.findByCycleUuid(cycleUuid);
    }

    public List<CourseResponseDTO> getCoursesByCycleAndCareer(UUID cycleUuid, UUID careerUuid) {
        List<CourseEntity> courses = courseRepository.findByCycleUuidAndCareerUuid(cycleUuid, careerUuid);
        return courseMapper.toResponseDTOList(courses);
    }

    @Transactional
    public CourseResponseDTO createCourse(CourseRequestDTO courseDTO) {
        // Verificar si ya existe un curso con el mismo nombre en el ciclo
        if (courseRepository.existsByNameAndCycleUuid(courseDTO.getName(), courseDTO.getCycleUuid())) {
            throw new IllegalArgumentException("Ya existe un curso con el mismo nombre en este ciclo");
        }

        // Obtener las entidades relacionadas
        CycleEntity cycle = cycleService.findCycleOrThrow(courseDTO.getCycleUuid());
        KnowledgeAreaEntity knowledgeArea = knowledgeAreaService.findKnowledgeAreaOrThrow(courseDTO.getKnowledgeAreaUuid());
        LearningSpaceSpecialtyEntity specialty = null;
        if (courseDTO.getPreferredSpecialtyUuid() != null) {
            specialty = specialtyService.findSpecialtyOrThrow(courseDTO.getPreferredSpecialtyUuid());
        }
        Set<TeachingTypeEntity> teachingTypes = getTeachingTypesFromUuids(courseDTO.getTeachingTypeUuids());

        // Crear y guardar el curso
        CourseEntity course = courseMapper.toEntity(courseDTO, cycle, knowledgeArea, specialty, teachingTypes);
        CourseEntity savedCourse = save(course);

        return courseMapper.toResponseDTO(savedCourse);
    }

    @Transactional
    public CourseResponseDTO updateCourse(UUID uuid, CourseRequestDTO courseDTO) {
        // Verificar que exista el curso
        CourseEntity course = findCourseOrThrow(uuid);

        // Verificar si hay otro curso (no este mismo) con el mismo nombre en el ciclo
        boolean existsAnotherCourse = courseRepository.findByCycleUuid(courseDTO.getCycleUuid()).stream()
                .anyMatch(c -> c.getName().equals(courseDTO.getName()) && !c.getUuid().equals(uuid));

        if (existsAnotherCourse) {
            throw new IllegalArgumentException("Ya existe otro curso con el mismo nombre en este ciclo");
        }

        // Obtener las entidades relacionadas
        CycleEntity cycle = cycleService.findCycleOrThrow(courseDTO.getCycleUuid());
        KnowledgeAreaEntity knowledgeArea = knowledgeAreaService.findKnowledgeAreaOrThrow(courseDTO.getKnowledgeAreaUuid());
        LearningSpaceSpecialtyEntity specialty = null;
        if (courseDTO.getPreferredSpecialtyUuid() != null) {
            specialty = specialtyService.findSpecialtyOrThrow(courseDTO.getPreferredSpecialtyUuid());
        }
        Set<TeachingTypeEntity> teachingTypes = getTeachingTypesFromUuids(courseDTO.getTeachingTypeUuids());

        // Actualizar el curso
        courseMapper.updateEntityFromDTO(course, courseDTO, cycle, knowledgeArea, specialty, teachingTypes);
        CourseEntity updatedCourse = save(course);

        return courseMapper.toResponseDTO(updatedCourse);
    }

    public List<CourseResponseDTO> filterCourses(CourseFilterDTO filters) {
        List<CourseEntity> courses = new ArrayList<>();

        // Aplicar filtros en orden de especificidad
        if (filters.getModalityUuid() != null) {
            if (filters.getCareerUuid() != null) {
                if (filters.getCycleUuid() != null) {
                    courses = courseRepository.findByCycleUuid(filters.getCycleUuid());
                } else {
                    courses = courseRepository.findByCareerUuid(filters.getCareerUuid());
                }
            } else {
                courses = courseRepository.findByModalityUuid(filters.getModalityUuid());
            }
        } else if (filters.getKnowledgeAreaUuid() != null) {
            courses = courseRepository.findByKnowledgeAreaUuid(filters.getKnowledgeAreaUuid());
        } else {
            courses = findAll();
        }

        // Filtrar por nombre si se especificó
        if (filters.getCourseName() != null && !filters.getCourseName().trim().isEmpty()) {
            final String searchTerm = filters.getCourseName().toLowerCase();
            courses = courses.stream()
                    .filter(c -> c.getName().toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
        }

        return courseMapper.toResponseDTOList(courses);
    }

    /**
     * Obtiene cursos por área de conocimiento
     */
    public List<CourseResponseDTO> getCoursesByKnowledgeArea(UUID knowledgeAreaUuid) {
        List<CourseEntity> courses = courseRepository.findByKnowledgeAreaUuid(knowledgeAreaUuid);
        return courseMapper.toResponseDTOList(courses);
    }

    private Set<TeachingTypeEntity> getTeachingTypesFromUuids(List<UUID> teachingTypeUuids) {
        return teachingTypeUuids.stream()
                .map(teachingTypeService::findTeachingTypeOrThrow)
                .collect(Collectors.toSet());
    }
}