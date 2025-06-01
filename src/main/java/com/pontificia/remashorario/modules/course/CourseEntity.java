package com.pontificia.remashorario.modules.course;

import com.pontificia.remashorario.modules.cycle.CycleEntity;
import com.pontificia.remashorario.modules.learningSpaceSpecialty.LearningSpaceSpecialtyEntity;
import com.pontificia.remashorario.modules.KnowledgeArea.KnowledgeAreaEntity;
import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "course")
@Getter
@Setter
public class CourseEntity extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private CycleEntity cycle;

    /** Área de conocimiento que el docente debe tener. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_area_id", nullable = false)
    private KnowledgeAreaEntity teachingKnowledgeArea;

    /** Horas teóricas semanales. */
    @Column(name = "weekly_theory_hours", nullable = false)
    private Integer weeklyTheoryHours;

    /** Horas prácticas semanales. */
    @Column(name = "weekly_practice_hours", nullable = false)
    private Integer weeklyPracticeHours;

    /** Especialidad de laboratorio preferida. Puede ser null. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_specialty_id", referencedColumnName = "uuid")
    private LearningSpaceSpecialtyEntity preferredSpecialty;


    @ManyToMany
    @JoinTable(
            name = "course_teaching_type",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "teaching_type_id")
    )
    private Set<TeachingTypeEntity> teachingTypes = new HashSet<>();
}
