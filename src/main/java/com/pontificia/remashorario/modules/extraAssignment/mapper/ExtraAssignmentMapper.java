package com.pontificia.remashorario.modules.extraAssignment.mapper;

import com.pontificia.remashorario.modules.attendanceActivityType.AttendanceActivityTypeEntity;
import com.pontificia.remashorario.modules.attendanceActivityType.mapper.AttendanceActivityTypeMapper;
import com.pontificia.remashorario.modules.extraAssignment.ExtraAssignmentEntity;
import com.pontificia.remashorario.modules.extraAssignment.dto.ExtraAssignmentRequestDTO;
import com.pontificia.remashorario.modules.extraAssignment.dto.ExtraAssignmentResponseDTO;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teacher.mapper.TeacherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExtraAssignmentMapper {

    private final AttendanceActivityTypeMapper activityTypeMapper;
    private final TeacherMapper teacherMapper;

    public ExtraAssignmentResponseDTO toResponseDTO(ExtraAssignmentEntity entity) {
        if (entity == null) return null;

        BigDecimal calculatedPayment = null;
        if (entity.getRatePerHour() != null && entity.getDurationMinutes() != null) {
            BigDecimal hours = BigDecimal.valueOf(entity.getDurationMinutes())
                    .divide(BigDecimal.valueOf(60), 4, BigDecimal.ROUND_HALF_UP);
            calculatedPayment = entity.getRatePerHour().multiply(hours).setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        return ExtraAssignmentResponseDTO.builder()
                .uuid(entity.getUuid())
                .teacher(teacherMapper.toResponseDTO(entity.getTeacher()))
                .activityType(activityTypeMapper.toResponseDTO(entity.getActivityType()))
                .title(entity.getTitle())
                .assignmentDate(entity.getAssignmentDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .durationMinutes(entity.getDurationMinutes())
                .ratePerHour(entity.getRatePerHour())
                .notes(entity.getNotes())
                .calculatedPayment(calculatedPayment)
                .build();
    }

    public List<ExtraAssignmentResponseDTO> toResponseDTOList(List<ExtraAssignmentEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ExtraAssignmentEntity toEntity(ExtraAssignmentRequestDTO dto, TeacherEntity teacher,
                                         AttendanceActivityTypeEntity activityType) {
        if (dto == null) return null;
        ExtraAssignmentEntity entity = new ExtraAssignmentEntity();
        entity.setTeacher(teacher);
        entity.setActivityType(activityType);
        entity.setTitle(dto.getTitle());
        entity.setAssignmentDate(dto.getAssignmentDate());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setRatePerHour(dto.getRatePerHour());
        entity.setNotes(dto.getNotes());
        // Duration is calculated in the service
        return entity;
    }

    public void updateEntityFromDTO(ExtraAssignmentEntity entity, ExtraAssignmentRequestDTO dto) {
        if (entity == null || dto == null) return;
        entity.setTitle(dto.getTitle());
        entity.setAssignmentDate(dto.getAssignmentDate());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setRatePerHour(dto.getRatePerHour());
        entity.setNotes(dto.getNotes());
        // Duration is recalculated in the service
    }
}
