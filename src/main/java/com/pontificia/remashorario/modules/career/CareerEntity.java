package com.pontificia.remashorario.modules.career;

import com.pontificia.remashorario.modules.cycle.CycleEntity;
import com.pontificia.remashorario.modules.educationalModality.EducationalModalityEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "career")
@Getter
@Setter
public class CareerEntity extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "modalidad_id", nullable = false)
    private EducationalModalityEntity modality;

    @OneToMany(mappedBy = "career", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CycleEntity> cycles = new ArrayList<>();
}
