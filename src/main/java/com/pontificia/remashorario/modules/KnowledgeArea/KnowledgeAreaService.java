package com.pontificia.remashorario.modules.KnowledgeArea;

import com.pontificia.remashorario.modules.KnowledgeArea.dto.KnowledgeAreaRequestDTO;
import com.pontificia.remashorario.modules.KnowledgeArea.dto.KnowledgeAreaResponseDTO;
import com.pontificia.remashorario.modules.KnowledgeArea.mapper.KnowledgeAreaMapper;
import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentEntity;
import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentService;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class KnowledgeAreaService extends BaseService<KnowledgeAreaEntity> {

    private final KnowledgeAreaRepository knowledgeAreaRepository;
    private final KnowledgeAreaMapper knowledgeAreaMapper;
    private final AcademicDepartmentService departmentService;

    @Autowired
    public KnowledgeAreaService(KnowledgeAreaRepository knowledgeAreaRepository,
                                KnowledgeAreaMapper knowledgeAreaMapper,
                                AcademicDepartmentService departmentService) {
        super(knowledgeAreaRepository);
        this.knowledgeAreaRepository = knowledgeAreaRepository;
        this.knowledgeAreaMapper = knowledgeAreaMapper;
        this.departmentService = departmentService;
    }

    public List<KnowledgeAreaResponseDTO> getAllKnowledgeAreas() {
        List<KnowledgeAreaEntity> areas = findAll();
        return knowledgeAreaMapper.toResponseDTOList(areas);
    }

    public List<KnowledgeAreaResponseDTO> getKnowledgeAreasByDepartment(UUID departmentUuid) {
        List<KnowledgeAreaEntity> areas = knowledgeAreaRepository.findByDepartmentUuid(departmentUuid);
        return knowledgeAreaMapper.toResponseDTOList(areas);
    }

    public KnowledgeAreaResponseDTO getKnowledgeAreaById(UUID uuid) {
        KnowledgeAreaEntity area = findKnowledgeAreaOrThrow(uuid);
        return knowledgeAreaMapper.toResponseDTO(area);
    }

    public KnowledgeAreaEntity findKnowledgeAreaOrThrow(UUID uuid) {
        return findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Área de conocimiento no encontrada con ID: " + uuid));
    }

    @Transactional
    public KnowledgeAreaResponseDTO createKnowledgeArea(KnowledgeAreaRequestDTO dto) {
        // Obtener el departamento
        AcademicDepartmentEntity department = departmentService.findDepartmentOrThrow(dto.getDepartmentUuid());

        // Verificar unicidad dentro del departamento
        if (knowledgeAreaRepository.existsByNameAndDepartmentUuid(dto.getName(), dto.getDepartmentUuid())) {
            throw new IllegalArgumentException("Ya existe un área de conocimiento con ese nombre en el departamento");
        }

        KnowledgeAreaEntity area = knowledgeAreaMapper.toEntity(dto, department);
        KnowledgeAreaEntity savedArea = save(area);

        return knowledgeAreaMapper.toResponseDTO(savedArea);
    }

    @Transactional
    public KnowledgeAreaResponseDTO updateKnowledgeArea(UUID uuid, KnowledgeAreaRequestDTO dto) {
        KnowledgeAreaEntity area = findKnowledgeAreaOrThrow(uuid);
        AcademicDepartmentEntity department = departmentService.findDepartmentOrThrow(dto.getDepartmentUuid());

        // Verificar unicidad si cambia el nombre o departamento
        boolean nameChanged = !area.getName().equals(dto.getName());
        boolean departmentChanged = !area.getDepartment().getUuid().equals(dto.getDepartmentUuid());

        if ((nameChanged || departmentChanged) &&
                knowledgeAreaRepository.existsByNameAndDepartmentUuid(dto.getName(), dto.getDepartmentUuid())) {
            throw new IllegalArgumentException("Ya existe un área de conocimiento con ese nombre en el departamento");
        }

        knowledgeAreaMapper.updateEntityFromDTO(area, dto, department);
        KnowledgeAreaEntity updatedArea = save(area);

        return knowledgeAreaMapper.toResponseDTO(updatedArea);
    }

    @Transactional
    public void deleteKnowledgeArea(UUID uuid) {
        KnowledgeAreaEntity area = findKnowledgeAreaOrThrow(uuid);

        // Verificar si tiene docentes asociados
        if (area.getTeachers() != null && !area.getTeachers().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el área porque tiene docentes asociados");
        }

        deleteById(uuid);
    }

    public List<KnowledgeAreaResponseDTO> searchByName(String name) {
        List<KnowledgeAreaEntity> areas = knowledgeAreaRepository.findByNameContainingIgnoreCase(name);
        return knowledgeAreaMapper.toResponseDTOList(areas);
    }
}
