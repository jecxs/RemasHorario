package com.pontificia.remashorario.modules.auth;

import com.pontificia.remashorario.config.security.JwtService;
import com.pontificia.remashorario.modules.user.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public String login(String email, String password) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return jwtService.generateToken(userDetails);
    }
}
