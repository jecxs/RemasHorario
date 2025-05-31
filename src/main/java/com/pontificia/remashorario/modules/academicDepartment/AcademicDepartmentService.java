package com.pontificia.remashorario.modules.academicDepartment;

import com.pontificia.remashorario.modules.academicDepartment.dto.AcademicDepartmentRequestDTO;
import com.pontificia.remashorario.modules.academicDepartment.dto.AcademicDepartmentResponseDTO;
import com.pontificia.remashorario.modules.academicDepartment.mapper.AcademicDepartmentMapper;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AcademicDepartmentService extends BaseService<AcademicDepartmentEntity> {

    private final AcademicDepartmentRepository departmentRepository;
    private final AcademicDepartmentMapper departmentMapper;

    @Autowired
    public AcademicDepartmentService(AcademicDepartmentRepository departmentRepository,
                                     AcademicDepartmentMapper departmentMapper) {
        super(departmentRepository);
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    public List<AcademicDepartmentResponseDTO> getAllDepartments() {
        List<AcademicDepartmentEntity> departments = findAll();
        return departmentMapper.toResponseDTOList(departments);
    }

    public AcademicDepartmentResponseDTO getDepartmentById(UUID uuid) {
        AcademicDepartmentEntity department = findDepartmentOrThrow(uuid);
        return departmentMapper.toResponseDTO(department);
    }

    public AcademicDepartmentEntity findDepartmentOrThrow(UUID uuid) {
        return findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Departamento académico no encontrado con ID: " + uuid));
    }

    @Transactional
    public AcademicDepartmentResponseDTO createDepartment(AcademicDepartmentRequestDTO dto) {
        // Verificar unicidad del nombre
        if (departmentRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Ya existe un departamento con el nombre: " + dto.getName());
        }

        // Verificar unicidad del código si se proporciona
        if (dto.getCode() != null && departmentRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Ya existe un departamento con el código: " + dto.getCode());
        }

        AcademicDepartmentEntity department = departmentMapper.toEntity(dto);
        AcademicDepartmentEntity savedDepartment = save(department);

        return departmentMapper.toResponseDTO(savedDepartment);
    }

    @Transactional
    public AcademicDepartmentResponseDTO updateDepartment(UUID uuid, AcademicDepartmentRequestDTO dto) {
        AcademicDepartmentEntity department = findDepartmentOrThrow(uuid);

        // Verificar unicidad del nombre si cambia
        if (!department.getName().equals(dto.getName()) &&
                departmentRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Ya existe un departamento con el nombre: " + dto.getName());
        }

        // Verificar unicidad del código si cambia
        if (dto.getCode() != null &&
                (department.getCode() == null || !department.getCode().equals(dto.getCode())) &&
                departmentRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Ya existe un departamento con el código: " + dto.getCode());
        }

        departmentMapper.updateEntityFromDTO(department, dto);
        AcademicDepartmentEntity updatedDepartment = save(department);

        return departmentMapper.toResponseDTO(updatedDepartment);
    }

    @Transactional
    public void deleteDepartment(UUID uuid) {
        AcademicDepartmentEntity department = findDepartmentOrThrow(uuid);

        // Verificar si tiene áreas de conocimiento asociadas
        if (department.getKnowledgeAreas() != null && !department.getKnowledgeAreas().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el departamento porque tiene áreas de conocimiento asociadas");
        }

        deleteById(uuid);
    }
}


