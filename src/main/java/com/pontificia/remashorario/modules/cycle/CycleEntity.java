package com.pontificia.remashorario.modules.cycle;

import com.pontificia.remashorario.modules.career.CareerEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cycle")
@Getter
@Setter
public class CycleEntity extends BaseEntity {
    @Column(nullable = false)
    private Integer number;

    @ManyToOne(optional = false)
    @JoinColumn(name = "career_id", nullable = false)
    private CareerEntity career;
}
