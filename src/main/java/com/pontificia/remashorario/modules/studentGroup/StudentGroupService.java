package com.pontificia.remashorario.modules.studentGroup;

import com.pontificia.remashorario.modules.studentGroup.dto.StudentGroupRequestDTO;
import com.pontificia.remashorario.modules.studentGroup.dto.StudentGroupResponseDTO;
import com.pontificia.remashorario.modules.studentGroup.mapper.StudentGroupMapper;
import com.pontificia.remashorario.utils.abstractBase.BaseService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StudentGroupService extends BaseService<StudentGroupEntity> {

    private final StudentGroupMapper studentGroupMapper;
    private final StudentGroupRepository studentGroupRepository; // Inyectar el repositorio directamente si necesitas métodos personalizados

    public StudentGroupService(StudentGroupRepository studentGroupRepository, StudentGroupMapper studentGroupMapper) {
        super(studentGroupRepository); // Pasa el repositorio a la clase base
        this.studentGroupMapper = studentGroupMapper;
        this.studentGroupRepository = studentGroupRepository;
    }

    /**
     * Obtiene todos los grupos de estudiantes y los convierte a un formato de respuesta (DTO).
     *
     * @return Lista de DTOs de respuesta de grupos de estudiantes.
     */
    public List<StudentGroupResponseDTO> getAllStudentGroups() {
        List<StudentGroupEntity> studentGroups = findAll(); // findAll viene de BaseService
        return studentGroupMapper.toResponseDTOList(studentGroups);
    }

    /**
     * Crea un nuevo grupo de estudiantes con los datos proporcionados en el DTO.
     * Realiza una validación para asegurar que el nombre del grupo sea único dentro de su ciclo.
     *
     * @param requestDTO DTO con los datos necesarios para crear el grupo.
     * @return DTO de respuesta con los detalles del grupo creado.
     * @throws IllegalArgumentException Si ya existe un grupo con el mismo nombre para el ciclo dado.
     */
    @Transactional
    public StudentGroupResponseDTO createStudentGroup(StudentGroupRequestDTO requestDTO) {
        // Validar si ya existe un grupo con el mismo nombre para el ciclo dado
        if (studentGroupRepository.existsByNameAndCycle_Uuid(requestDTO.getName(), requestDTO.getCycleUuid())) {
            throw new IllegalArgumentException("Ya existe un grupo con el nombre '" + requestDTO.getName() + "' para el ciclo especificado.");
        }

        StudentGroupEntity studentGroup = studentGroupMapper.toEntity(requestDTO);
        StudentGroupEntity savedStudentGroup = save(studentGroup); // save viene de BaseService

        return studentGroupMapper.toResponseDTO(savedStudentGroup);
    }

    /**
     * Actualiza un grupo de estudiantes existente con los datos proporcionados en el DTO.
     * Realiza una validación para asegurar que el nombre del grupo sea único dentro de su ciclo,
     * excluyendo el grupo actual si el nombre no ha cambiado.
     *
     * @param uuid       UUID del grupo de estudiantes a actualizar.
     * @param requestDTO DTO con los nuevos datos del grupo.
     * @return DTO de respuesta con los detalles del grupo actualizado.
     * @throws IllegalArgumentException Si ya existe un grupo con el mismo nombre para el ciclo dado.
     */
    @Transactional
    public StudentGroupResponseDTO updateStudentGroup(UUID uuid, StudentGroupRequestDTO requestDTO) {
        StudentGroupEntity existingStudentGroup = findOrThrow(uuid); // findOrThrow viene de BaseService

        // Validar si el nombre ha cambiado y si ya existe otro grupo con ese nombre para el mismo ciclo
        if (!requestDTO.getName().equals(existingStudentGroup.getName()) ||
                !requestDTO.getCycleUuid().equals(existingStudentGroup.getCycle().getUuid())) {
            if (studentGroupRepository.existsByNameAndCycle_Uuid(requestDTO.getName(), requestDTO.getCycleUuid())) {
                throw new IllegalArgumentException("Ya existe otro grupo con el nombre '" + requestDTO.getName() + "' para el ciclo especificado.");
            }
        }

        studentGroupMapper.updateEntityFromDTO(requestDTO, existingStudentGroup);
        StudentGroupEntity updatedStudentGroup = update(existingStudentGroup); // update viene de BaseService

        return studentGroupMapper.toResponseDTO(updatedStudentGroup);
    }

    /**
     * Elimina un grupo de estudiantes dado su UUID.
     *
     * @param uuid UUID del grupo de estudiantes a eliminar.
     */
    @Transactional
    public void deleteStudentGroup(UUID uuid) {
        findOrThrow(uuid); // findOrThrow viene de BaseService
        deleteById(uuid); // deleteById viene de BaseService
    }

    /**
     * Busca un grupo de estudiantes por su UUID y lo convierte a DTO de respuesta.
     *
     * @param uuid UUID del grupo de estudiantes.
     * @return DTO de respuesta del grupo de estudiantes.
     */
    public StudentGroupResponseDTO getStudentGroupByUuid(UUID uuid) {
        StudentGroupEntity studentGroup = findOrThrow(uuid);
        return studentGroupMapper.toResponseDTO(studentGroup);
    }
}
