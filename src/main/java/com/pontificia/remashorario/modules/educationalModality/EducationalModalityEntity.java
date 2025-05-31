package com.pontificia.remashorario.modules.educationalModality;

import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "educational_modality")
@Getter
@Setter
public class EducationalModalityEntity extends BaseEntity {

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(nullable = false, name = "duration_years")
    private Integer durationYears;

    @Lob
    private String description;
}

