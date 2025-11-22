package com.pontificia.remashorario.modules.teacherRate.mapper;

import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeEntity;
import com.pontificia.remashorario.modules.attendanceActivityType.mapper.AttendanceActivityTypeMapper;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teacher.mapper.TeacherMapper;
import com.pontificia.remashorario.modules.teacherRate.TeacherRateEntity;
import com.pontificia.remashorario.modules.teacherRate.dto.TeacherRateRequestDTO;
import com.pontificia.remashorario.modules.teacherRate.dto.TeacherRateResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeacherRateMapper {

    private final AttendanceActivityTypeMapper activityTypeMapper;
    private final TeacherMapper teacherMapper;

    public TeacherRateResponseDTO toResponseDTO(TeacherRateEntity entity) {
        if (entity == null) return null;

        boolean isActive = isRateActive(entity);

        return TeacherRateResponseDTO.builder()
                .uuid(entity.getUuid())
                .teacher(teacherMapper.toResponseDTO(entity.getTeacher()))
                .activityType(activityTypeMapper.toResponseDTO(entity.getActivityType()))
                .ratePerHour(entity.getRatePerHour())
                .effectiveFrom(entity.getEffectiveFrom())
                .effectiveTo(entity.getEffectiveTo())
                .isActive(isActive)
                .build();
    }

    public List<TeacherRateResponseDTO> toResponseDTOList(List<TeacherRateEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public TeacherRateEntity toEntity(TeacherRateRequestDTO dto, TeacherEntity teacher,
                                      AttendanceActivityTypeEntity activityType) {
        if (dto == null) return null;
        TeacherRateEntity entity = new TeacherRateEntity();
        entity.setTeacher(teacher);
        entity.setActivityType(activityType);
        entity.setRatePerHour(dto.getRatePerHour());
        entity.setEffectiveFrom(dto.getEffectiveFrom());
        entity.setEffectiveTo(dto.getEffectiveTo());
        return entity;
    }

    public void updateEntityFromDTO(TeacherRateEntity entity, TeacherRateRequestDTO dto) {
        if (entity == null || dto == null) return;
        entity.setRatePerHour(dto.getRatePerHour());
        entity.setEffectiveFrom(dto.getEffectiveFrom());
        entity.setEffectiveTo(dto.getEffectiveTo());
    }

    private boolean isRateActive(TeacherRateEntity entity) {
        LocalDate today = LocalDate.now();
        return (entity.getEffectiveFrom().isBefore(today) || entity.getEffectiveFrom().equals(today)) &&
               (entity.getEffectiveTo() == null || entity.getEffectiveTo().isAfter(today) || entity.getEffectiveTo().equals(today));
    }
}
