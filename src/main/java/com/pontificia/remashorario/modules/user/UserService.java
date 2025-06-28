package com.pontificia.remashorario.modules.user;

import com.pontificia.remashorario.utils.abstractBase.BaseService;
import com.pontificia.remashorario.modules.teacher.TeacherEntity;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService extends BaseService<UserEntity> implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super(userRepository);
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        // ✅ VERIFICAR: Que los roles se estén asignando correctamente
        System.out.println("=== DEBUG USER ROLES ===");
        System.out.println("User: " + user.getEmail());
        System.out.println("Role: " + user.getRole());
        System.out.println("Role name: " + user.getRole().name());
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(!user.getActive())
                .build();
    }

    public UserEntity findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
    }

    public UserEntity createTeacherUser(TeacherEntity teacher, String rawPassword) {
        if (userRepository.existsByEmail(teacher.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + teacher.getEmail());
        }
        UserEntity user = new UserEntity();
        user.setEmail(teacher.getEmail());
        user.setFullName(teacher.getFullName());
        user.setRole(UserEntity.UserRole.TEACHER);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        user.setFirstLogin(true);
        user.setTeacher(teacher);
        UserEntity saved = userRepository.save(user);
        teacher.setUser(saved);
        teacher.setHasUserAccount(true);
        return saved;
    }

    public void updateUserStatus(UUID userUuid, boolean active) {
        UserEntity user = findOrThrow(userUuid);
        user.setActive(active);
        userRepository.save(user);
    }
}
