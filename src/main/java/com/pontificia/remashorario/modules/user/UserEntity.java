package com.pontificia.remashorario.modules.user;

import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import com.pontificia.remashorario.utils.abstractBase.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password; // Será hasheada con BCrypt

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserEntity.UserRole role;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean firstLogin = true; // Para futuras mejoras

    // Relación bidireccional con Teacher
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private TeacherEntity teacher;

    public enum UserRole {
        COORDINATOR("Coordinador General"),
        ASSISTANT("Administrador"),
        TEACHER("Docente");

        private final String displayName;

        UserRole(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
