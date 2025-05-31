package com.pontificia.remashorario.modules.teacher;

import com.pontificia.remashorario.modules.KnowledgeArea.KnowledgeAreaEntity;
import com.pontificia.remashorario.modules.KnowledgeArea.KnowledgeAreaService;
import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentEntity;
import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentService;
import com.pontificia.remashorario.modules.teacher.dto.TeacherFilterDTO;
import com.pontificia.remashorario.modules.teacher.dto.TeacherRequestDTO;
import com.pontificia.remashorario.modules.teacher.dto.TeacherResponseDTO;
import com.pontificia.remashorario.modules.teacher.dto.TeacherUpdateDTO;
import com.pontificia.remashorario.modules.teacher.mapper.TeacherMapper;

import com.pontificia.remashorario.modules.teacherAvailability.dto.TeacherWithAvailabilitiesDTO;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherService extends BaseService<TeacherEntity> {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;
    private final AcademicDepartmentService departmentService;
    private final KnowledgeAreaService knowledgeAreaService;

    @Autowired
    public TeacherService(TeacherRepository teacherRepository,
                          TeacherMapper teacherMapper,
                          AcademicDepartmentService departmentService,
                          KnowledgeAreaService knowledgeAreaService) {
        super(teacherRepository);
        this.teacherRepository = teacherRepository;
        this.teacherMapper = teacherMapper;
        this.departmentService = departmentService;
        this.knowledgeAreaService = knowledgeAreaService;
    }

    public List<TeacherResponseDTO> getAllTeachers() {
        List<TeacherEntity> teachers = findAll();
        return teacherMapper.toResponseDTOList(teachers);
    }

    public TeacherResponseDTO getTeacherById(UUID uuid) {
        TeacherEntity teacher = findTeacherOrThrow(uuid);
        return teacherMapper.toResponseDTO(teacher);
    }

    public TeacherWithAvailabilitiesDTO getTeacherWithAvailabilities(UUID uuid) {
        TeacherEntity teacher = teacherRepository.findByIdWithAvailabilities(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Docente no encontrado con ID: " + uuid));
        return teacherMapper.toWithAvailabilitiesDTO(teacher);
    }

    public TeacherEntity findTeacherOrThrow(UUID uuid) {
        return findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Docente no encontrado con ID: " + uuid));
    }

    @Transactional
    public TeacherResponseDTO createTeacher(TeacherRequestDTO dto) {
        // Verificar email único
        if (teacherRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un docente con el email: " + dto.getEmail());
        }

        // Obtener entidades relacionadas
        AcademicDepartmentEntity department = departmentService.findDepartmentOrThrow(dto.getDepartmentUuid());
        Set<KnowledgeAreaEntity> knowledgeAreas = getKnowledgeAreasFromUuids(dto.getKnowledgeAreaUuids());

        // Validar que las áreas de conocimiento pertenezcan al departamento
        validateKnowledgeAreasBelongToDepartment(knowledgeAreas, department);

        // Crear y guardar
        TeacherEntity teacher = teacherMapper.toEntity(dto, department, knowledgeAreas);
        TeacherEntity savedTeacher = save(teacher);

        return teacherMapper.toResponseDTO(savedTeacher);
    }

    @Transactional
    public TeacherResponseDTO updateTeacher(UUID uuid, TeacherUpdateDTO dto) {
        TeacherEntity teacher = findTeacherOrThrow(uuid);

        // Obtener entidades relacionadas
        AcademicDepartmentEntity department = departmentService.findDepartmentOrThrow(dto.getDepartmentUuid());
        Set<KnowledgeAreaEntity> knowledgeAreas = getKnowledgeAreasFromUuids(dto.getKnowledgeAreaUuids());

        // Validar que las áreas de conocimiento pertenezcan al departamento
        validateKnowledgeAreasBelongToDepartment(knowledgeAreas, department);

        // Actualizar
        teacherMapper.updateEntityFromDTO(teacher, dto, department, knowledgeAreas);
        TeacherEntity updatedTeacher = save(teacher);

        return teacherMapper.toResponseDTO(updatedTeacher);
    }

    @Transactional
    public void deleteTeacher(UUID uuid) {
        TeacherEntity teacher = findTeacherOrThrow(uuid);

        // Verificar si tiene asignaciones de horario (cuando se implemente)
        // TODO: Verificar asignaciones cuando se implemente ScheduleAssignment

        // Si tiene cuenta de usuario, solo desactivar
        if (teacher.getHasUserAccount()) {
            throw new IllegalStateException("No se puede eliminar un docente con cuenta de usuario activa");
        }

        deleteById(uuid);
    }

    public List<TeacherResponseDTO> filterTeachers(TeacherFilterDTO filters) {
        List<TeacherEntity> teachers;

        if (filters.getDepartmentUuid() != null && filters.getKnowledgeAreaUuids() != null && !filters.getKnowledgeAreaUuids().isEmpty()) {
            teachers = teacherRepository.findByDepartmentAndKnowledgeAreas(
                    filters.getDepartmentUuid(), filters.getKnowledgeAreaUuids());
        } else if (filters.getDepartmentUuid() != null) {
            teachers = teacherRepository.findByDepartmentUuid(filters.getDepartmentUuid());
        } else if (filters.getKnowledgeAreaUuids() != null && !filters.getKnowledgeAreaUuids().isEmpty()) {
            teachers = teacherRepository.findByKnowledgeAreaUuids(filters.getKnowledgeAreaUuids());
        } else if (filters.getSearchTerm() != null && !filters.getSearchTerm().trim().isEmpty()) {
            teachers = teacherRepository.searchByNameOrEmail(filters.getSearchTerm());
        } else if (filters.getHasUserAccount() != null) {
            teachers = teacherRepository.findByHasUserAccount(filters.getHasUserAccount());
        } else {
            teachers = findAll();
        }

        return teacherMapper.toResponseDTOList(teachers);
    }

    public List<TeacherResponseDTO> getTeachersByDepartment(UUID departmentUuid) {
        List<TeacherEntity> teachers = teacherRepository.findByDepartmentUuid(departmentUuid);
        return teacherMapper.toResponseDTOList(teachers);
    }

    /**
     * Obtiene docentes sugeridos para un curso basándose en el departamento
     */
    public List<TeacherResponseDTO> getSuggestedTeachersForCourse(UUID courseUuid) {
        // TODO: Implementar cuando se tenga la relación Course-Department
        // Por ahora devuelve todos los docentes
        return getAllTeachers();
    }

    private Set<KnowledgeAreaEntity> getKnowledgeAreasFromUuids(List<UUID> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return new HashSet<>();
        }
        return uuids.stream()
                .map(knowledgeAreaService::findKnowledgeAreaOrThrow)
                .collect(Collectors.toSet());
    }

    private void validateKnowledgeAreasBelongToDepartment(Set<KnowledgeAreaEntity> knowledgeAreas,
                                                          AcademicDepartmentEntity department) {
        for (KnowledgeAreaEntity area : knowledgeAreas) {
            if (!area.getDepartment().getUuid().equals(department.getUuid())) {
                throw new IllegalArgumentException(
                        "El área de conocimiento '" + area.getName() +
                                "' no pertenece al departamento seleccionado");
            }
        }
    }
}

