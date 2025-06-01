package com.pontificia.remashorario.modules.learningSpaceSpecialty;

import com.pontificia.remashorario.modules.academicDepartment.AcademicDepartmentEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "learning_space_specialty")
@Getter
@Setter
public class LearningSpaceSpecialtyEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name; // p.ej. "LAB_COMPUTING", "LAB_ENFERMERIA"

    @Column(length = 255)
    private String description;

    /** Opcional: departamento al que pertenece el laboratorio. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private AcademicDepartmentEntity department;
}
