package com.pontificia.remashorario.modules.academicCalendarException;

import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "academic_calendar_exception")
@Getter
@Setter
public class AcademicCalendarExceptionEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(nullable = false, length = 50)
    private String code;

    @Lob
    private String description;
}
