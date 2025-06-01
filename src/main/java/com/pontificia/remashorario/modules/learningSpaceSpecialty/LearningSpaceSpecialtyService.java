package com.pontificia.remashorario.modules.learningSpaceSpecialty;

import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentEntity;
import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentService;
import com.pontificia.remashorario.modules.learningSpaceSpecialty.dto.LearningSpaceSpecialtyRequestDTO;
import com.pontificia.remashorario.modules.learningSpaceSpecialty.dto.LearningSpaceSpecialtyResponseDTO;
import com.pontificia.remashorario.modules.learningSpaceSpecialty.mapper.LearningSpaceSpecialtyMapper;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LearningSpaceSpecialtyService extends BaseService<LearningSpaceSpecialtyEntity> {

    private final LearningSpaceSpecialtyRepository specialtyRepository;
    private final LearningSpaceSpecialtyMapper specialtyMapper;
    private final AcademicDepartmentService departmentService;

    @Autowired
    public LearningSpaceSpecialtyService(LearningSpaceSpecialtyRepository specialtyRepository,
                                         LearningSpaceSpecialtyMapper specialtyMapper,
                                         AcademicDepartmentService departmentService) {
        super(specialtyRepository);
        this.specialtyRepository = specialtyRepository;
        this.specialtyMapper = specialtyMapper;
        this.departmentService = departmentService;
    }

    public List<LearningSpaceSpecialtyResponseDTO> getAllSpecialties() {
        List<LearningSpaceSpecialtyEntity> list = findAll();
        return specialtyMapper.toResponseDTOList(list);
    }

    public LearningSpaceSpecialtyResponseDTO getSpecialtyById(UUID uuid) {
        LearningSpaceSpecialtyEntity entity = findSpecialtyOrThrow(uuid);
        return specialtyMapper.toResponseDTO(entity);
    }

    public LearningSpaceSpecialtyEntity findSpecialtyOrThrow(UUID uuid) {
        return findById(uuid).orElseThrow(() -> new EntityNotFoundException("Especialidad no encontrada con ID: " + uuid));
    }

    @Transactional
    public LearningSpaceSpecialtyResponseDTO createSpecialty(LearningSpaceSpecialtyRequestDTO dto) {
        AcademicDepartmentEntity dept = null;
        if (dto.getDepartmentUuid() != null) {
            dept = departmentService.findDepartmentOrThrow(dto.getDepartmentUuid());
        }
        LearningSpaceSpecialtyEntity entity = specialtyMapper.toEntity(dto, dept);
        LearningSpaceSpecialtyEntity saved = save(entity);
        return specialtyMapper.toResponseDTO(saved);
    }

    @Transactional
    public LearningSpaceSpecialtyResponseDTO updateSpecialty(UUID uuid, LearningSpaceSpecialtyRequestDTO dto) {
        LearningSpaceSpecialtyEntity entity = findSpecialtyOrThrow(uuid);
        AcademicDepartmentEntity dept = null;
        if (dto.getDepartmentUuid() != null) {
            dept = departmentService.findDepartmentOrThrow(dto.getDepartmentUuid());
        }
        specialtyMapper.updateEntityFromDTO(entity, dto, dept);
        LearningSpaceSpecialtyEntity updated = save(entity);
        return specialtyMapper.toResponseDTO(updated);
    }

    @Transactional
    public void deleteSpecialty(UUID uuid) {
        findSpecialtyOrThrow(uuid);
        deleteById(uuid);
    }
}
