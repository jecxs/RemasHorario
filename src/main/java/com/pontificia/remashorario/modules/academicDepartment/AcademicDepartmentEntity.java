package com.pontificia.remashorario.modules.academicDepartment;

import com.pontificia.remashorario.modules.KnowledgeArea.KnowledgeAreaEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "academic_department")
@Getter
@Setter
public class AcademicDepartmentEntity  extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(unique = true, length = 10)
    private String code;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private Set<KnowledgeAreaEntity> knowledgeAreas = new HashSet<>();
}
