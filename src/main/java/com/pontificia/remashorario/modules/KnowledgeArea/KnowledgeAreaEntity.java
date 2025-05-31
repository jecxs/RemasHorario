package com.pontificia.remashorario.modules.KnowledgeArea;

import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentEntity;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "knowledge_area")
@Getter
@Setter
public class KnowledgeAreaEntity extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private AcademicDepartmentEntity department;

    @ManyToMany(mappedBy = "knowledgeAreas")
    private Set<TeacherEntity> teachers = new HashSet<>();
}
