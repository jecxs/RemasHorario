package com.pontificia.remashorario.modules.classSession;

import com.pontificia.remashorario.modules.TimeSlot.TimeSlotEntity;
import com.pontificia.remashorario.modules.TimeSlot.TimeSlotRepository;
import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentEntity;
import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentRepository;
import com.pontificia.remashorario.modules.career.CareerEntity;
import com.pontificia.remashorario.modules.career.CareerRepository;
import com.pontificia.remashorario.modules.course.CourseEntity;
import com.pontificia.remashorario.modules.course.CourseRepository;
import com.pontificia.remashorario.modules.cycle.CycleEntity;
import com.pontificia.remashorario.modules.cycle.CycleRepository;
import com.pontificia.remashorario.modules.educationalModality.EducationalModalityEntity;
import com.pontificia.remashorario.modules.educationalModality.EducationalModalityRepository;
import com.pontificia.remashorario.modules.KnowledgeArea.KnowledgeAreaEntity;
import com.pontificia.remashorario.modules.KnowledgeArea.KnowledgeAreaRepository;
import com.pontificia.remashorario.modules.learningSpace.LearningSpaceEntity;
import com.pontificia.remashorario.modules.learningSpace.LearningSpaceRepository;
import com.pontificia.remashorario.modules.period.PeriodEntity;
import com.pontificia.remashorario.modules.period.PeriodRepository;
import com.pontificia.remashorario.modules.studentGroup.StudentGroupEntity;
import com.pontificia.remashorario.modules.studentGroup.StudentGroupRepository;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.modules.teacher.TeacherRepository;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourEntity;
import com.pontificia.remashorario.modules.teachingHour.TeachingHourRepository;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ClassSessionRepositoryTest {

    @Autowired
    private ClassSessionRepository classSessionRepository;
    @Autowired
    private TeachingHourRepository teachingHourRepository;
    @Autowired
    private TimeSlotRepository timeSlotRepository;
    @Autowired
    private StudentGroupRepository studentGroupRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private LearningSpaceRepository learningSpaceRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private TeachingTypeRepository teachingTypeRepository;
    @Autowired
    private PeriodRepository periodRepository;
    @Autowired
    private CycleRepository cycleRepository;
    @Autowired
    private CareerRepository careerRepository;
    @Autowired
    private EducationalModalityRepository educationalModalityRepository;
    @Autowired
    private AcademicDepartmentRepository academicDepartmentRepository;
    @Autowired
    private KnowledgeAreaRepository knowledgeAreaRepository;

    private TeachingHourEntity createTeachingHour() {
        TimeSlotEntity slot = new TimeSlotEntity();
        slot.setName("M");
        slot.setStartTime(LocalTime.of(8,0));
        slot.setEndTime(LocalTime.of(12,0));
        slot = timeSlotRepository.save(slot);

        TeachingHourEntity th = new TeachingHourEntity();
        th.setOrderInTimeSlot(1);
        th.setStartTime(LocalTime.of(8,0));
        th.setEndTime(LocalTime.of(9,0));
        th.setDurationMinutes(60);
        th.setTimeSlot(slot);
        return teachingHourRepository.save(th);
    }

    private TeachingTypeEntity createTeachingType() {
        TeachingTypeEntity type = new TeachingTypeEntity();
        type.setName(TeachingTypeEntity.ETeachingType.THEORY);
        return teachingTypeRepository.save(type);
    }

    private CareerEntity createCareer(EducationalModalityEntity modality) {
        CareerEntity career = new CareerEntity();
        career.setName("Ing");
        career.setModality(modality);
        return careerRepository.save(career);
    }

    private CycleEntity createCycle(CareerEntity career) {
        CycleEntity cycle = new CycleEntity();
        cycle.setNumber(1);
        cycle.setCareer(career);
        return cycleRepository.save(cycle);
    }

    private CourseEntity createCourse(CycleEntity cycle, KnowledgeAreaEntity ka, TeachingTypeEntity type) {
        CourseEntity course = new CourseEntity();
        course.setName("Course");
        course.setCode("C1");
        course.setCycle(cycle);
        course.setTeachingKnowledgeArea(ka);
        course.setWeeklyTheoryHours(2);
        course.setWeeklyPracticeHours(0);
        course.getTeachingTypes().add(type);
        return courseRepository.save(course);
    }

    private TeacherEntity createTeacher(AcademicDepartmentEntity dept) {
        TeacherEntity teacher = new TeacherEntity();
        teacher.setFullName("Docente");
        teacher.setEmail("d@example.com");
        teacher.setDepartment(dept);
        return teacherRepository.save(teacher);
    }

    private LearningSpaceEntity createLearningSpace(TeachingTypeEntity type) {
        LearningSpaceEntity ls = new LearningSpaceEntity();
        ls.setName("Aula 1");
        ls.setCapacity(30);
        ls.setTypeUUID(type);
        return learningSpaceRepository.save(ls);
    }

    private StudentGroupEntity createGroup(CycleEntity cycle, PeriodEntity period) {
        StudentGroupEntity sg = new StudentGroupEntity();
        sg.setName("G1");
        sg.setCycle(cycle);
        sg.setPeriod(period);
        return studentGroupRepository.save(sg);
    }

    private PeriodEntity createPeriod(String name) {
        PeriodEntity period = new PeriodEntity();
        period.setName(name);
        period.setStartDate(LocalDate.now());
        period.setEndDate(LocalDate.now().plusMonths(4));
        return periodRepository.save(period);
    }

    private AcademicDepartmentEntity createDepartment() {
        AcademicDepartmentEntity dept = new AcademicDepartmentEntity();
        dept.setName("Dep");
        dept.setCode("D1");
        return academicDepartmentRepository.save(dept);
    }

    private KnowledgeAreaEntity createKnowledgeArea(AcademicDepartmentEntity dept) {
        KnowledgeAreaEntity ka = new KnowledgeAreaEntity();
        ka.setName("KA");
        ka.setDepartment(dept);
        return knowledgeAreaRepository.save(ka);
    }

    @Test
    void conflictQueriesIgnoreDifferentPeriods() {
        EducationalModalityEntity modality = new EducationalModalityEntity();
        modality.setName("Pres");
        modality.setDurationYears(5);
        modality.setDescription("desc");
        educationalModalityRepository.save(modality);

        CareerEntity career = createCareer(modality);
        CycleEntity cycle = createCycle(career);

        AcademicDepartmentEntity dept = createDepartment();
        KnowledgeAreaEntity ka = createKnowledgeArea(dept);

        TeachingTypeEntity type = createTeachingType();
        CourseEntity course = createCourse(cycle, ka, type);
        TeacherEntity teacher = createTeacher(dept);
        LearningSpaceEntity space = createLearningSpace(type);
        TeachingHourEntity th = createTeachingHour();

        PeriodEntity period1 = createPeriod("2024-1");
        PeriodEntity period2 = createPeriod("2024-2");

        StudentGroupEntity group1 = createGroup(cycle, period1);
        StudentGroupEntity group2 = createGroup(cycle, period2);

        ClassSessionEntity s1 = new ClassSessionEntity();
        s1.setStudentGroup(group1);
        s1.setCourse(course);
        s1.setTeacher(teacher);
        s1.setLearningSpace(space);
        s1.setSessionType(type);
        s1.setDayOfWeek(DayOfWeek.MONDAY);
        s1.setPeriod(period1);
        s1.getTeachingHours().add(th);
        classSessionRepository.save(s1);

        ClassSessionEntity s2 = new ClassSessionEntity();
        s2.setStudentGroup(group2);
        s2.setCourse(course);
        s2.setTeacher(teacher);
        s2.setLearningSpace(space);
        s2.setSessionType(type);
        s2.setDayOfWeek(DayOfWeek.MONDAY);
        s2.setPeriod(period2);
        s2.getTeachingHours().add(th);
        classSessionRepository.save(s2);

        List<UUID> hourIds = List.of(th.getUuid());
        List<ClassSessionEntity> teacherConflicts = classSessionRepository.findTeacherConflicts(
                teacher.getUuid(), DayOfWeek.MONDAY, period1.getUuid(), hourIds);
        assertThat(teacherConflicts).hasSize(1);
        assertThat(teacherConflicts.get(0).getPeriod().getUuid()).isEqualTo(period1.getUuid());

        List<ClassSessionEntity> groupConflicts = classSessionRepository.findStudentGroupConflicts(
                group1.getUuid(), DayOfWeek.MONDAY, period1.getUuid(), hourIds);
        assertThat(groupConflicts).hasSize(1);
        assertThat(groupConflicts.get(0).getStudentGroup().getUuid()).isEqualTo(group1.getUuid());
    }
}