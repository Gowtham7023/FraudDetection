package com.fraud.api_gateway.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fraud.api_gateway.security.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final String configuredUsername;
    private final String configuredPassword;

    public AuthController(
            JwtService jwtService,
            @Value("${AUTH_USERNAME}") String configuredUsername,
            @Value("${AUTH_PASSWORD}") String configuredPassword) {
        this.jwtService = jwtService;
        this.configuredUsername = configuredUsername;
        this.configuredPassword = configuredPassword;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request == null
                || !configuredUsername.equals(request.username())
                || !configuredPassword.equals(request.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials");
        }

        return ResponseEntity.ok(new LoginResponse(
                jwtService.issueToken(request.username()),
                "Bearer",
                jwtService.getExpirationSeconds()));
    }
}
