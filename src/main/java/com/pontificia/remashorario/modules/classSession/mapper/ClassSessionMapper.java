package com.pontificia.remashorario.modules.classSession.mapper;

import com.pontificia.remashorario.modules.classSession.ClassSessionEntity;
import com.pontificia.remashorario.modules.classSession.dto.ClassSessionRequestDTO;
import com.pontificia.remashorario.modules.classSession.dto.ClassSessionResponseDTO;
import com.pontificia.remashorario.modules.course.CourseEntity;
import com.pontificia.remashorario.modules.course.mapper.CourseMapper;
import com.pontificia.remashorario.modules.learningSpace.LearningSpaceEntity;
import com.pontificia.remashorario.modules.learningSpace.mapper.LearningSpaceMapper;
import com.pontificia.remashorario.modules.studentGroup.StudentGroupEntity;
import com.pontificia.remashorario.modules.studentGroup.mapper.StudentGroupMapper;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teacher.mapper.TeacherMapper;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import com.pontificia.remashorario.modules.teachingHour.mapper.TeachingHourMapper;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.modules.teachingType.mapper.TeachingTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClassSessionMapper {

    private final StudentGroupMapper studentGroupMapper;
    private final CourseMapper courseMapper;
    private final TeacherMapper teacherMapper;
    private final LearningSpaceMapper learningSpaceMapper;
    private final TeachingTypeMapper teachingTypeMapper;
    private final TeachingHourMapper teachingHourMapper;

    @Autowired
    public ClassSessionMapper(StudentGroupMapper studentGroupMapper,
                              CourseMapper courseMapper,
                              TeacherMapper teacherMapper,
                              LearningSpaceMapper learningSpaceMapper,
                              TeachingTypeMapper teachingTypeMapper,
                              TeachingHourMapper teachingHourMapper) {
        this.studentGroupMapper = studentGroupMapper;
        this.courseMapper = courseMapper;
        this.teacherMapper = teacherMapper;
        this.learningSpaceMapper = learningSpaceMapper;
        this.teachingTypeMapper = teachingTypeMapper;
        this.teachingHourMapper = teachingHourMapper;
    }

    public ClassSessionResponseDTO toResponseDTO(ClassSessionEntity entity) {
        if (entity == null) return null;

        // Obtener el nombre del turno de la primera hora pedagógica
        String timeSlotName = entity.getTeachingHours().stream()
                .findFirst()
                .map(th -> th.getTimeSlot().getName())
                .orElse(null);

        return ClassSessionResponseDTO.builder()
                .uuid(entity.getUuid())
                .studentGroup(studentGroupMapper.toResponseDTO(entity.getStudentGroup()))
                .course(courseMapper.toResponseDTO(entity.getCourse()))
                .teacher(teacherMapper.toResponseDTO(entity.getTeacher()))
                .learningSpace(learningSpaceMapper.toResponseDTO(entity.getLearningSpace()))
                .dayOfWeek(entity.getDayOfWeek())
                .sessionType(teachingTypeMapper.toResponseDTO(entity.getSessionType()))
                .teachingHours(entity.getTeachingHours().stream()
                        .map(teachingHourMapper::toTeachingHourResponseDTO)
                        .sorted((h1, h2) -> Integer.compare(h1.getOrderInTimeSlot(), h2.getOrderInTimeSlot()))
                        .collect(Collectors.toList()))
                .notes(entity.getNotes())
                .totalHours(entity.getTeachingHours().size())
                .timeSlotName(timeSlotName)
                .build();
    }

    public List<ClassSessionResponseDTO> toResponseDTOList(List<ClassSessionEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ClassSessionEntity toEntity(ClassSessionRequestDTO dto,
                                       StudentGroupEntity studentGroup,
                                       CourseEntity course,
                                       TeacherEntity teacher,
                                       LearningSpaceEntity learningSpace,
                                       TeachingTypeEntity sessionType,
                                       Set<TeachingHourEntity> teachingHours) {
        if (dto == null) return null;

        ClassSessionEntity entity = new ClassSessionEntity();
        entity.setStudentGroup(studentGroup);
        entity.setCourse(course);
        entity.setTeacher(teacher);
        entity.setLearningSpace(learningSpace);
        entity.setDayOfWeek(dto.getDayOfWeek());
        entity.setSessionType(sessionType);
        entity.setTeachingHours(teachingHours);
        entity.setNotes(dto.getNotes());

        entity.setPeriod(studentGroup.getPeriod());

        return entity;
    }

    public void updateEntityFromDTO(ClassSessionEntity entity,
                                    ClassSessionRequestDTO dto,
                                    StudentGroupEntity studentGroup,
                                    CourseEntity course,
                                    TeacherEntity teacher,
                                    LearningSpaceEntity learningSpace,
                                    TeachingTypeEntity sessionType,
                                    Set<TeachingHourEntity> teachingHours) {
        if (entity == null || dto == null) return;

        entity.setStudentGroup(studentGroup);
        entity.setCourse(course);
        entity.setTeacher(teacher);
        entity.setLearningSpace(learningSpace);
        entity.setDayOfWeek(dto.getDayOfWeek());
        entity.setSessionType(sessionType);
        entity.clearTeachingHours();
        entity.setTeachingHours(teachingHours);
        entity.setNotes(dto.getNotes());
    }
}
