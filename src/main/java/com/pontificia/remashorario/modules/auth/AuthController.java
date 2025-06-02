package com.pontificia.remashorario.modules.auth;

import com.pontificia.remashorario.config.ApiResponse;
import com.pontificia.remashorario.modules.auth.dto.LoginRequestDTO;
import com.pontificia.remashorario.modules.auth.dto.LoginResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(new LoginResponseDTO(token), "Inicio de sesión exitoso"));
    }
}
