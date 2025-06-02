package com.pontificia.remashorario.modules.user;

import com.pontificia.remashorario.config.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/protected/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/{uuid}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(@PathVariable UUID uuid,
                                                              @RequestParam boolean active) {
        userService.updateUserStatus(uuid, active);
        return ResponseEntity.ok(ApiResponse.success(null, "Estado actualizado"));
    }
}
