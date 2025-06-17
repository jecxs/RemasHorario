package com.pontificia.remashorario.modules.schedule;

import com.pontificia.remashorario.modules.classSession.ClassSessionService;
import com.pontificia.remashorario.modules.classSession.dto.ClassSessionRequestDTO;
import com.pontificia.remashorario.modules.course.CourseEntity;
import com.pontificia.remashorario.modules.course.CourseService;
import com.pontificia.remashorario.modules.learningSpace.LearningSpaceEntity;
import com.pontificia.remashorario.modules.learningSpace.LearningSpaceService;
import com.pontificia.remashorario.modules.studentGroup.StudentGroupEntity;
import com.pontificia.remashorario.modules.studentGroup.StudentGroupService;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teacher.TeacherRepository;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourRepository;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity.ETeachingType;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleGenerationService {

    private final StudentGroupService studentGroupService;
    private final CourseService courseService;
    private final TeacherRepository teacherRepository;
    private final LearningSpaceService learningSpaceService;
    private final ClassSessionService classSessionService;
    private final TeachingHourRepository teachingHourRepository;

    public ScheduleGenerationService(StudentGroupService studentGroupService,
                                     CourseService courseService,
                                     TeacherRepository teacherRepository,
                                     LearningSpaceService learningSpaceService,
                                     ClassSessionService classSessionService,
                                     TeachingHourRepository teachingHourRepository) {
        this.studentGroupService = studentGroupService;
        this.courseService = courseService;
        this.teacherRepository = teacherRepository;
        this.learningSpaceService = learningSpaceService;
        this.classSessionService = classSessionService;
        this.teachingHourRepository = teachingHourRepository;
    }

    /**
     * Genera horarios automáticos para todas las asignaturas de un grupo.
     * El algoritmo es básico y asigna horas de forma incremental buscando el
     * primer docente, aula y hora disponibles que cumplan las restricciones.
     */
    @Transactional
    public void generateForGroup(UUID studentGroupUuid) {
        StudentGroupEntity group = studentGroupService.findOrThrow(studentGroupUuid);
        List<CourseEntity> courses = courseService.getCoursesByCycle(group.getCycle().getUuid());
        List<TeachingHourEntity> allHours = teachingHourRepository.findAll().stream()
                .sorted(Comparator.comparing(TeachingHourEntity::getStartTime))
                .collect(Collectors.toList());

        for (CourseEntity course : courses) {
            for (TeachingTypeEntity type : course.getTeachingTypes()) {
                int hoursNeeded = type.getName() == ETeachingType.THEORY ?
                        course.getWeeklyTheoryHours() : course.getWeeklyPracticeHours();
                if (hoursNeeded <= 0) continue;
                allocateHours(group, course, type, hoursNeeded, allHours);
            }
        }
    }

    private void allocateHours(StudentGroupEntity group,
                               CourseEntity course,
                               TeachingTypeEntity type,
                               int hoursNeeded,
                               List<TeachingHourEntity> orderedHours) {
        List<TeacherEntity> teachers = teacherRepository.findByKnowledgeAreaUuids(
                Collections.singletonList(course.getTeachingKnowledgeArea().getUuid()));
        UUID specialty = type.getName() == ETeachingType.PRACTICE && course.getPreferredSpecialty() != null ?
                course.getPreferredSpecialty().getUuid() : null;
        List<LearningSpaceEntity> spaces = learningSpaceService.findEntitiesByTypeAndSpecialty(type.getName(), specialty);

        if (teachers.isEmpty() || spaces.isEmpty()) {
            throw new IllegalStateException("No hay docentes o aulas disponibles para " + course.getName());
        }

        int assigned = 0;
        OUTER:
        while (assigned < hoursNeeded) {
            for (DayOfWeek day : EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)) {
                for (TeachingHourEntity hour : orderedHours) {
                    for (TeacherEntity teacher : teachers) {
                        for (LearningSpaceEntity space : spaces) {
                            ClassSessionRequestDTO dto = new ClassSessionRequestDTO();
                            dto.setStudentGroupUuid(group.getUuid());
                            dto.setCourseUuid(course.getUuid());
                            dto.setTeacherUuid(teacher.getUuid());
                            dto.setLearningSpaceUuid(space.getUuid());
                            dto.setDayOfWeek(day);
                            dto.setSessionTypeUuid(type.getUuid());
                            dto.setTeachingHourUuids(Collections.singletonList(hour.getUuid()));
                            try {
                                classSessionService.createClassSession(dto);
                                assigned++;
                                if (assigned >= hoursNeeded) {
                                    break OUTER;
                                }
                                // Continuar con siguiente hora
                            } catch (Exception ignored) {
                                // Si hay conflicto o docente no disponible, se prueba con otra combinación
                            }
                        }
                    }
                }
            }
            if (assigned < hoursNeeded) {
                throw new IllegalStateException("No se pudo asignar todas las horas para " + course.getName());
            }
        }
    }
}
