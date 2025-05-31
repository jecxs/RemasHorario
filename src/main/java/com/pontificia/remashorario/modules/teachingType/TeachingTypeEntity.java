package com.pontificia.remashorario.modules.teachingType;

import com.pontificia.remashorario.modules.course.CourseEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "teaching_Type_Entity")
@Getter
@Setter
public class TeachingTypeEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, unique = true)
    private TeachingTypeEntity.ETeachingType name;

    @ManyToMany(mappedBy = "teachingTypes")
    private Set<CourseEntity> courses = new HashSet<>();

    public enum ETeachingType {
        THEORY,
        PRACTICE
    }
}
