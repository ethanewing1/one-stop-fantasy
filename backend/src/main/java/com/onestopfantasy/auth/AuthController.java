package com.onestopfantasy.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;

    record AuthRequest(String email, String password) {}
    record AuthResponse(String token) {}

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest req) {
        String token = authService.register(req.email(), req.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        String token = authService.login(req.email(), req.password());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
