package com.pontificia.remashorario.modules.attendanceActivityType;

import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "attendance_activity_type")
@Getter
@Setter
public class AttendanceActivityTypeEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Lob
    private String description;
}
