package com.pontificia.remashorario.modules.learningSpace;

import com.pontificia.remashorario.modules.teachingType.TeachingTypeEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "learning_space")
@Getter
@Setter
public class LearningSpaceEntity extends BaseEntity {
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @ManyToOne
    @JoinColumn(name = "type_uuid", referencedColumnName = "uuid")
    private TeachingTypeEntity typeUUID;
}
